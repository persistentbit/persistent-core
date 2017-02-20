package com.persistentbit.core.parser;

import com.persistentbit.core.parser.source.Position;
import com.persistentbit.core.utils.BaseValueClass;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 20/02/2017
 */
public class WithPos<T> extends BaseValueClass{
    public final Position   pos;
    public final T          value;

    public WithPos(Position pos, T value) {
        this.pos = pos;
        this.value = value;
    }

    @Override
    public String toString() {
        return "WithPos{" +
                "pos=" + pos +
                ", value=" + value +
                '}';
    }
}
