package com.rmn.pairwise;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class MoleculeTests {
    @Test
    public void testEquals() {
        List<Integer> intArray = Arrays.asList(0, 1);
        Molecule molecule1 = new Molecule(intArray);
        Molecule molecule2 = new Molecule(intArray);
        
        Assert.assertEquals( "The two test molecule objects, set to reference the same int array, should be equal", molecule1, molecule2 );
    }

    @Test
    public void testDoesNotEqual() {
        List<Integer> intArray = Arrays.asList(0, 1);
        Molecule molecule1 = new Molecule(intArray);
        Molecule molecule2 = new Molecule(Arrays.asList(1, 0));
        
        Assert.assertFalse("The two test molecules, set to reference different int arrays (with the same values) shoud not be equal", molecule1.equals(molecule2));
    }

    @Test
    public void testAtomsPerMolecule() {
        Molecule molecule = new Molecule(Arrays.asList(0, 1, 2, 3, 4, 5, 6));
        
        Assert.assertEquals("There should be 7 atoms in this molecule", 7, molecule.getAtomsPerMolecule());
    }

    @Test
    public void testAtomsPerMoleculeConstructor() {
        Molecule molecule = new Molecule(7);
        Assert.assertNull("There should be no atoms in the molecule at first", molecule.getAtoms());
        Assert.assertEquals("There should be 7 atoms in this molecule", 7, molecule.getAtomsPerMolecule());
    }

    @Test
    public void testUpdateAtomsPerMoleculeCount() {
        Molecule molecule = new Molecule(7);
        Assert.assertEquals(7, molecule.getAtomsPerMolecule());
        
        molecule.setAtoms(Arrays.asList(0, 1, 2));
        Assert.assertEquals(3, molecule.getAtomsPerMolecule());
    }

    @Test
    public void testDoesNotEqualFirstNull() {
        Molecule molecule1 = new Molecule(null);
        Molecule molecule2 = new Molecule(Arrays.asList(1, 0));
        
        Assert.assertFalse(molecule1.equals(molecule2));
    }

    @Test
    public void testDoesNotEqualSecondNull() {
        Molecule molecule1 = new Molecule(Arrays.asList(1, 0));
        Molecule molecule2 = new Molecule(null);
        
        Assert.assertFalse(molecule1.equals(molecule2));
    }
}
