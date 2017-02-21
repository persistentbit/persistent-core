package com.persistentbit.core.parser.source;

/**
 * TODO: Add comment
 *
 * @author Peter Muys
 * @since 21/02/2017
 */
public class SourcePlusSource extends Source{
    private final Source left;
    private final Source right;

    public SourcePlusSource(Source left, Source right) {
        super(left.position, left.current);
        this.left = left;
        this.right = right;
    }

    @Override
    public Source next() {
        Source nl = left.next();
        if(nl.isEOF() == false){
            return new SourcePlusSource(nl,right);
        }
        return right;
    }

    @Override
    public String rest() {
        return left.rest() + right.rest();
    }
}
