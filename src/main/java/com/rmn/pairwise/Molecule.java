package com.rmn.pairwise;

/**
 * Represents a single "molecule" of data. Not a test set, not a parameter set, but the smallest "piece" of the scenario you want to test. It 
 * is analagous to a "pair" in the "pairwise" terminology, but I didn't want to be limited to 2 "atoms". Also note that a molecule doesn't
 * represents the values themselves--it represents the indices of the one-dimensional array the Scenario uses to keep track of ALL atoms
 * @author mmerrell
 */
public class Molecule {
    public Molecule( int[] atoms ) {
        this.setAtoms( atoms );
    }
    
    public Molecule( int atomsPerMolecule ) {
        this.setAtomsPerMolecule( atomsPerMolecule );
    }

    private int atomsPerMolecule;
    public int getAtomsPerMolecule() { return atomsPerMolecule; }
    public void setAtomsPerMolecule( int atomsPerMolecule ) { this.atomsPerMolecule = atomsPerMolecule; }

    private int[] atoms;
    public int[] getAtoms() { return atoms; }
    public void setAtoms( int[] atoms ) { 
        this.atoms = atoms;
        if ( null != atoms ) {
            this.setAtomsPerMolecule( atoms.length );
        }
    }
    
    private boolean used = false;
    public boolean isUsed() { return used; }
    public void setUsed( boolean used ) { this.used = used; }
    
    private boolean illegal = false;
    public boolean isIllegal() { return illegal; }
    public void setIllegal( boolean illegal ) { this.illegal = illegal; }
    
    @Override
    public boolean equals( Object o ) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Molecule that = ( Molecule ) o;

        if (atoms != null ? !atoms.equals(that.getAtoms()) : that.getAtoms() != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return atoms != null ? atoms.hashCode() : 0;
    }
}
