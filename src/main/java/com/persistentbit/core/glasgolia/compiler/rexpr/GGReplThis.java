package com.persistentbit.core.glasgolia.compiler.rexpr;

import com.persistentbit.core.Lazy;
import com.persistentbit.core.glasgolia.compiler.frames.ReplCompileFrame;
import com.persistentbit.core.utils.StrPos;
import com.persistentbit.core.utils.ToDo;

import java.util.Optional;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 16/03/2017
 */
public class GGReplThis implements GGObject{

    private ReplCompileFrame    frame;
    private final Lazy<RExpr> thisRExpr;

    @Override
    public String toString() {
        return "GGReplThis[]";
    }

    public GGReplThis(ReplCompileFrame    frame) {
        this.frame = frame;
        this.thisRExpr = new Lazy<>(() -> new RExpr(){
            @Override
            public Class getType() {
                return GGReplThis.class;
            }

            @Override
            public StrPos getPos() {
                return StrPos.inst;
            }

            @Override
            public boolean isConst() {
                return true;
            }

            @Override
            public Object get() {
                return GGReplThis.this;
            }
        });
    }


    @Override
    public Object assignChild(String childName, Object value) {
        return frame.assignChild(childName,value);
    }

    public RExpr asRExpr() {
        return thisRExpr.get();
    }

    public Optional<RExpr> bindChild(String name) {
        try{
            return Optional.ofNullable(frame.bind(StrPos.inst,name));
        }catch(Exception e){
            return Optional.empty();
        }

    }

    @Override
    public Object getChild(StrPos pos, String name) {
        return frame.bind(pos,name).get();
    }


    @Override
    public Object binOp(StrPos pos, String op, Object other) {
        throw new ToDo();
    }

    @Override
    public Object castTo(StrPos pos, Class cls) {
        throw new ToDo();
    }



}
