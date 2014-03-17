package com.rmn.pairwise;

import java.util.List;
import java.util.Set;

/**
 * User: mmerrell
 * Date: 12/5/13
 */
public interface IModelConstraint {
    void addExclusion(Set<Integer> valuesToExclude);
    List<Set<Integer>> getExclusions();
}
