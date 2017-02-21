package com.persistentbit.core.parser.source;

import com.persistentbit.core.utils.StrPos;

import java.util.Objects;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/02/2017
 */
public class SourceFromString extends Source{
    private final String source;
    private final int sourcePos;
    public SourceFromString(String source, int sourcePos, StrPos position) {
        super(position,sourcePos < source.length()
                ? source.charAt(sourcePos)
                : EOF);
        this.source = Objects.requireNonNull(source);
        this.sourcePos = sourcePos;
    }


    public boolean isEOF() {
        return sourcePos >= source.length();
    }

    public Source next() {
        if(isEOF()) {
            return this;
        }
        return new SourceFromString(source, sourcePos + 1, position.incForChar(current));
    }

    public Source next(int count) {
        Source res = this;
        for(int t = 0; t < count; t++) {
            res = res.next();
        }
        return res;
    }

    public String rest() {
        return isEOF() ? "" : source.substring(sourcePos);
    }


}
