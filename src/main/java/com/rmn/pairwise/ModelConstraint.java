package com.rmn.pairwise;

import java.util.List;
import java.util.Set;

/**
 * User: mmerrell
 * Date: 12/5/13
 */
public class ModelConstraint implements IModelConstraint {
    private List<Set<Integer>> exclusions;

    @Override
    public void addExclusion(Set<Integer> valuesToExclude) {
        exclusions.add(valuesToExclude);
    }

    @Override
    public List<Set<Integer>> getExclusions() {
        return exclusions;
    }
}
