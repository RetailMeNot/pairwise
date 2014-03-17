package com.rmn.pairwise;

import org.junit.Assert;
import org.junit.Test;

public class MoleculeTests {

    @Test
    public void testEquals() {
        int[] intArray = new int[] { 0, 1 };
        Molecule molecule1 = new Molecule(intArray);
        Molecule molecule2 = new Molecule(intArray);
        
        Assert.assertEquals( "The two test molecule objects, set to reference the same int array, should be equal", molecule1, molecule2 );
    }

    @Test
    public void testDoesNotEqual() {
        int[] intArray = new int[] { 0, 1 };
        Molecule molecule1 = new Molecule(intArray);
        Molecule molecule2 = new Molecule(new int[] { 1, 0 });
        
        Assert.assertFalse("The two test molecules, set to reference different int arrays (with the same values) shoud not be equal", molecule1.equals(molecule2));
    }

    @Test
    public void testAtomsPerMolecule() {
        Molecule molecule = new Molecule(new int[] { 0, 1, 2, 3, 4, 5, 6 });
        
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
        
        molecule.setAtoms(new int[] { 0, 1, 2 });
        Assert.assertEquals(3, molecule.getAtomsPerMolecule());
    }

    @Test
    public void testDoesNotEqualFirstNull() {
        Molecule molecule1 = new Molecule(null);
        Molecule molecule2 = new Molecule(new int[] { 1, 0 });
        
        Assert.assertFalse(molecule1.equals(molecule2));
    }

    @Test
    public void testDoesNotEqualSecondNull() {
        Molecule molecule1 = new Molecule(new int[] { 1, 0 });
        Molecule molecule2 = new Molecule(null);
        
        Assert.assertFalse(molecule1.equals(molecule2));
    }
}
