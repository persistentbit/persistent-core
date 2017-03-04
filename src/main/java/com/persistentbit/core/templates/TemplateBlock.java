package com.persistentbit.core.templates;

import com.persistentbit.core.collections.PList;
import com.persistentbit.core.logging.Log;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.UString;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/02/2017
 */
public class TemplateBlock {
    public enum Type { code, string}
    private final String content;
    private final Type type;
    private final StrPos pos;

    public TemplateBlock(StrPos pos, String content, Type type) {
        this.content = Objects.requireNonNull(content);
        this.type = Objects.requireNonNull(type);
        this.pos = Objects.requireNonNull(pos);
    }

    public String getContent() {
        return content;
    }

    public Type getType() {
        return type;
    }
    public StrPos getPos() {
        return pos;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TemplateBlock that = (TemplateBlock) o;

        if (!content.equals(that.content)) return false;
        if (type != that.type) return false;
        return pos.equals(that.pos);
    }

    @Override
    public int hashCode() {
        int result = content.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + pos.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "TemplateBlock{" +
                "type=" + type +
                ", " + pos +
                ", content='" +
			UString.escapeToJavaString(
				UString.present(content, 40)
			)+ '\'' +

                '}';
    }

    public static PList<TemplateBlock> parse(String code){
        return parse(StrPos.inst,code);
    }

    public static PList<TemplateBlock> parse(StrPos pos, String code){
        return parse(pos,code,"<<",">>");
    }
    public static PList<TemplateBlock> parse(StrPos orgPos, String orgCode, String regExItemBlockStart, String regExItemBlockEnd){
		return Log.function(orgPos, UString.present(orgCode, 10), regExItemBlockStart, regExItemBlockEnd).code(l -> {
			Pattern p = Pattern.compile("(" + regExItemBlockStart + ")(.*?)(" + regExItemBlockEnd + ")" , Pattern.DOTALL);
            String code = orgCode;
            StrPos pos = orgPos;
            PList<TemplateBlock> res = PList.empty();
            while(code.isEmpty() == false){
                Matcher m = p.matcher(code);
                if(m.find()){
                    int start = m.start();
                    if(start > 0){
                        String printBlock = code.substring(0,start);
                        res = res.plus(new TemplateBlock(pos,printBlock, TemplateBlock.Type.string));
                        pos = pos.incForString(printBlock);
                    }

                    String g1 = m.group(1);
                    String g2 = m.group(2);
                    String g3 = m.group(3);
                    pos = pos.incForString(g1);
                    res = res.plus(new TemplateBlock(pos,g2, TemplateBlock.Type.code));
                    pos = pos.incForString(g2);
                    pos = pos.incForString(g3);
                    code = code.substring(m.end());
                }else {
                    res = res.plus(new TemplateBlock(pos, code, TemplateBlock.Type.string));
                    pos = pos.incForString(code);
                    code = "";
                }
            }
            return res;
        });

    }
}
