/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com).
 * All rights reserved. Use is subject to license terms and conditions.
 */
package au.csiro.snorocket.core;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import au.csiro.ontology.Node;
import au.csiro.ontology.axioms.ConceptInclusion;
import au.csiro.ontology.axioms.IAxiom;
import au.csiro.ontology.model.Concept;
import au.csiro.ontology.model.Conjunction;
import au.csiro.ontology.model.Datatype;
import au.csiro.ontology.model.Existential;
import au.csiro.ontology.model.Feature;
import au.csiro.ontology.model.FloatLiteral;
import au.csiro.ontology.model.IConcept;
import au.csiro.ontology.model.IntegerLiteral;
import au.csiro.ontology.model.Operator;
import au.csiro.ontology.model.Role;
import au.csiro.ontology.model.StringLiteral;

/**
 * Unit test cases for Snorocket concrete domains functionality.
 * 
 * @author Alejandro Metke
 * 
 */
public class TestConcreteDomains {

    /**
     * Very simple concrete domains test that uses equality and integers. The
     * expected taxonomy is:
     * 
     * -Thing
     *   -Bottle
     *   -Panadol
     *     -Panadol_250mg
     *       -Panadol_pack_250mg
     *     -Panadol_500mg
     *   -Paracetamol
     * 
     */
    //@Test
    public void testConcreteDomainsEqualityInts() {
        IFactory factory = new CoreFactory();

        // Add roles
        Role container = new Role("container");
        Role contains = new Role("contains");

        // Add features
        Feature mgPerTablet = new Feature("mgPerTablet");

        // Add concepts
        Concept panadol = new Concept("Panadol");
        Concept panadol_250mg = new Concept("Panadol_250mg");
        Concept panadol_500mg = new Concept("Panadol_500mg");
        Concept panadol_pack_250mg = 
                new Concept("Panadol_pack_250mg");
        Concept paracetamol = new Concept("Paracetamol");
        Concept bottle = new Concept("Bottle");

        // Add axioms
        ConceptInclusion a1 = new ConceptInclusion(panadol, 
                new Existential(contains, paracetamol));

        ConceptInclusion a2 = new ConceptInclusion(panadol_250mg,
                new Conjunction(new IConcept[] {
                        panadol,
                        new Datatype(mgPerTablet, Operator.EQUALS,
                                new IntegerLiteral(250)) }));

        ConceptInclusion a3 = new ConceptInclusion(new Conjunction(
                new IConcept[] {
                        panadol,
                        new Datatype(mgPerTablet, Operator.EQUALS,
                                new IntegerLiteral(250)) }), panadol_250mg);

        ConceptInclusion a4 = new ConceptInclusion(panadol_500mg,
                new Conjunction(new IConcept[] {
                        panadol,
                        new Datatype(mgPerTablet, Operator.EQUALS,
                                new IntegerLiteral(500)) }));

        ConceptInclusion a5 = new ConceptInclusion(new Conjunction(
                new IConcept[] {
                        panadol,
                        new Datatype(mgPerTablet, Operator.EQUALS,
                                new IntegerLiteral(500)) }), panadol_500mg);

        ConceptInclusion a6 = new ConceptInclusion(panadol_pack_250mg,
                new Conjunction(new IConcept[] {
                        panadol,
                        new Datatype(mgPerTablet, Operator.EQUALS,
                                new IntegerLiteral(250)),
                        new Existential(container, bottle) }));

        Set<IAxiom> axioms = new HashSet<IAxiom>();
        axioms.add(a1);
        axioms.add(a2);
        axioms.add(a3);
        axioms.add(a4);
        axioms.add(a5);
        axioms.add(a6);

        // Classify
        NormalisedOntology o = 
                new NormalisedOntology(factory, axioms);
        o.classify();

        // Build taxonomy
        o.buildTaxonomy();

        // Test results
        Node panadolNode = o.getEquivalents(panadol.getId());
        Set<Node> panadolRes = panadolNode.getParents();
        assertTrue(panadolRes.size() == 1);
        assertTrue(panadolRes.contains(o.getTopNode()));

        Node panadol_250mgNode = o.getEquivalents(panadol_250mg.getId());
        Set<Node> panadol_250mgRes = panadol_250mgNode.getParents();
        assertTrue(panadol_250mgRes.size() == 1);
        assertTrue(panadol_250mgRes
                .contains(o.getEquivalents(panadol.getId())));

        Node panadol_500mgNode = o.getEquivalents(panadol_500mg.getId());
        Set<Node> panadol_500mgRes = panadol_500mgNode.getParents();
        assertTrue(panadol_500mgRes.size() == 1);
        assertTrue(panadol_500mgRes
                .contains(o.getEquivalents(panadol.getId())));

        Node panadol_pack_250mgNode = o
                .getEquivalents(panadol_pack_250mg.getId());
        Set<Node> panadol_pack_250mgRes = panadol_pack_250mgNode
                .getParents();
        assertTrue(panadol_pack_250mgRes.size() == 1);
        assertTrue(panadol_pack_250mgRes.contains(o
                .getEquivalents(panadol_250mg.getId())));

        Node paracetamolNode = o.getEquivalents(paracetamol.getId());
        Set<Node> paracetamolRes = paracetamolNode.getParents();
        assertTrue(paracetamolRes.size() == 1);
        assertTrue(paracetamolRes.contains(o.getTopNode()));

        Node bottleNode = o.getEquivalents(bottle.getId());
        Set<Node> bottleRes = bottleNode.getParents();
        assertTrue(bottleRes.size() == 1);
        assertTrue(bottleRes.contains(o.getTopNode()));
    }

    /**
     * Very simple concrete domains test that uses equality and floats. The
     * expected taxonomy is:
     * 
     * -Thing
     *   -Bottle
     *   -Panadol
     *     -Panadol_250mg
     *       -Panadol_pack_250mg
     *     -Panadol_500mg
     *   -Paracetamol
     * 
     */
    //@Test
    public void testConcreteDomainsEqualityFloats() {
        IFactory factory = new CoreFactory();

        // Add roles
        Role container = new Role("container");
        Role contains = new Role("contains");

        // Add features
        Feature mgPerTablet = new Feature("mgPerTablet");

        // Add concepts
        Concept panadol = new Concept("Panadol");
        Concept panadol_250mg = new Concept("Panadol_250mg");
        Concept panadol_500mg = new Concept("Panadol_500mg");
        Concept panadol_pack_250mg = 
                new Concept("Panadol_pack_250mg");
        Concept paracetamol = new Concept("Paracetamol");
        Concept bottle = new Concept("Bottle");

        // Add axioms
        ConceptInclusion a1 = new ConceptInclusion(panadol, 
                new Existential(contains, paracetamol));

        ConceptInclusion a2 = new ConceptInclusion(panadol_250mg,
                new Conjunction(new IConcept[] {
                        panadol,
                        new Datatype(mgPerTablet, Operator.EQUALS,
                                new FloatLiteral(250.0f)) }));

        ConceptInclusion a3 = new ConceptInclusion(new Conjunction(
                new IConcept[] {
                        panadol,
                        new Datatype(mgPerTablet, Operator.EQUALS,
                                new FloatLiteral(250.0f)) }), panadol_250mg);

        ConceptInclusion a4 = new ConceptInclusion(panadol_500mg,
                new Conjunction(new IConcept[] {
                        panadol,
                        new Datatype(mgPerTablet, Operator.EQUALS,
                                new FloatLiteral(500.0f)) }));

        ConceptInclusion a5 = new ConceptInclusion(new Conjunction(
                new IConcept[] {
                        panadol,
                        new Datatype(mgPerTablet, Operator.EQUALS,
                                new FloatLiteral(500.0f)) }), panadol_500mg);

        ConceptInclusion a6 = new ConceptInclusion(panadol_pack_250mg,
                new Conjunction(new IConcept[] {
                        panadol,
                        new Datatype(mgPerTablet, Operator.EQUALS,
                                new FloatLiteral(250.0f)),
                        new Existential(container, bottle) }));

        Set<IAxiom> axioms = new HashSet<IAxiom>();
        axioms.add(a1);
        axioms.add(a2);
        axioms.add(a3);
        axioms.add(a4);
        axioms.add(a5);
        axioms.add(a6);

        // Classify
        NormalisedOntology o = 
                new NormalisedOntology(factory, axioms);
        o.classify();

        // Build taxonomy
        o.buildTaxonomy();

        // Test results
        Node panadolNode = o.getEquivalents(panadol.getId());
        Set<Node> panadolRes = panadolNode.getParents();
        assertTrue(panadolRes.size() == 1);
        assertTrue(panadolRes.contains(o.getTopNode()));

        Node panadol_250mgNode = o.getEquivalents(panadol_250mg.getId());
        Set<Node> panadol_250mgRes = panadol_250mgNode.getParents();
        assertTrue(panadol_250mgRes.size() == 1);
        assertTrue(panadol_250mgRes
                .contains(o.getEquivalents(panadol.getId())));

        Node panadol_500mgNode = o.getEquivalents(panadol_500mg.getId());
        Set<Node> panadol_500mgRes = panadol_500mgNode.getParents();
        assertTrue(panadol_500mgRes.size() == 1);
        assertTrue(panadol_500mgRes
                .contains(o.getEquivalents(panadol.getId())));

        Node panadol_pack_250mgNode = o
                .getEquivalents(panadol_pack_250mg.getId());
        Set<Node> panadol_pack_250mgRes = panadol_pack_250mgNode
                .getParents();
        assertTrue(panadol_pack_250mgRes.size() == 1);
        assertTrue(panadol_pack_250mgRes.contains(o
                .getEquivalents(panadol_250mg.getId())));

        Node paracetamolNode = o.getEquivalents(paracetamol.getId());
        Set<Node> paracetamolRes = paracetamolNode.getParents();
        assertTrue(paracetamolRes.size() == 1);
        assertTrue(paracetamolRes.contains(o.getTopNode()));

        Node bottleNode = o.getEquivalents(bottle.getId());
        Set<Node> bottleRes = bottleNode.getParents();
        assertTrue(bottleRes.size() == 1);
        assertTrue(bottleRes.contains(o.getTopNode()));
    }

    /**
     * Very simple concrete domains test that uses equality and strings. The
     * expected taxonomy is:
     * 
     * -Thing
     *   -Bottle
     *   -Panadol
     *     -Panadol_250mg
     *       -Panadol_pack_250mg
     *     -Panadol_500mg
     *   -Paracetamol
     * 
     */
    //@Test
    public void testConcreteDomainsEqualityStrings() {
        IFactory factory = new CoreFactory();

        // Add roles
        Role container = new Role("container");
        Role contains = new Role("contains");

        // Add features
        Feature mgPerTablet = new Feature("mgPerTablet");

        // Add concepts
        Concept panadol = new Concept("Panadol");
        Concept panadol_250mg = new Concept("Panadol_250mg");
        Concept panadol_500mg = new Concept("Panadol_500mg");
        Concept panadol_pack_250mg = 
                new Concept("Panadol_pack_250mg");
        Concept paracetamol = new Concept("Paracetamol");
        Concept bottle = new Concept("Bottle");

        // Add axioms
        ConceptInclusion a1 = new ConceptInclusion(panadol, 
                new Existential(contains, paracetamol));

        ConceptInclusion a2 = new ConceptInclusion(panadol_250mg,
                new Conjunction(new IConcept[] {
                        panadol,
                        new Datatype(mgPerTablet, Operator.EQUALS,
                                new StringLiteral("250")) }));

        ConceptInclusion a3 = new ConceptInclusion(new Conjunction(
                new IConcept[] {
                        panadol,
                        new Datatype(mgPerTablet, Operator.EQUALS,
                                new StringLiteral("250")) }), panadol_250mg);

        ConceptInclusion a4 = new ConceptInclusion(panadol_500mg,
                new Conjunction(new IConcept[] {
                        panadol,
                        new Datatype(mgPerTablet, Operator.EQUALS,
                                new StringLiteral("500")) }));

        ConceptInclusion a5 = new ConceptInclusion(new Conjunction(
                new IConcept[] {
                        panadol,
                        new Datatype(mgPerTablet, Operator.EQUALS,
                                new StringLiteral("500")) }), panadol_500mg);

        ConceptInclusion a6 = new ConceptInclusion(panadol_pack_250mg,
                new Conjunction(new IConcept[] {
                        panadol,
                        new Datatype(mgPerTablet, Operator.EQUALS,
                                new StringLiteral("250")),
                        new Existential(container, bottle) }));

        Set<IAxiom> axioms = new HashSet<IAxiom>();
        axioms.add(a1);
        axioms.add(a2);
        axioms.add(a3);
        axioms.add(a4);
        axioms.add(a5);
        axioms.add(a6);

        // Classify
        NormalisedOntology o = 
                new NormalisedOntology(factory, axioms);
        o.classify();

        // Build taxonomy
        o.buildTaxonomy();

        // Test results
        Node panadolNode = o.getEquivalents(panadol.getId());
        Set<Node> panadolRes = panadolNode.getParents();
        assertTrue(panadolRes.size() == 1);
        assertTrue(panadolRes.contains(o.getTopNode()));

        Node panadol_250mgNode = o.getEquivalents(panadol_250mg.getId());
        Set<Node> panadol_250mgRes = panadol_250mgNode.getParents();
        assertTrue(panadol_250mgRes.size() == 1);
        assertTrue(panadol_250mgRes
                .contains(o.getEquivalents(panadol.getId())));

        Node panadol_500mgNode = o.getEquivalents(panadol_500mg.getId());
        Set<Node> panadol_500mgRes = panadol_500mgNode.getParents();
        assertTrue(panadol_500mgRes.size() == 1);
        assertTrue(panadol_500mgRes
                .contains(o.getEquivalents(panadol.getId())));

        Node panadol_pack_250mgNode = o
                .getEquivalents(panadol_pack_250mg.getId());
        Set<Node> panadol_pack_250mgRes = panadol_pack_250mgNode
                .getParents();
        assertTrue(panadol_pack_250mgRes.size() == 1);
        assertTrue(panadol_pack_250mgRes.contains(o
                .getEquivalents(panadol_250mg.getId())));

        Node paracetamolNode = o.getEquivalents(paracetamol.getId());
        Set<Node> paracetamolRes = paracetamolNode.getParents();
        assertTrue(paracetamolRes.size() == 1);
        assertTrue(paracetamolRes.contains(o.getTopNode()));

        Node bottleNode = o.getEquivalents(bottle.getId());
        Set<Node> bottleRes = bottleNode.getParents();
        assertTrue(bottleRes.size() == 1);
        assertTrue(bottleRes.contains(o.getTopNode()));
    }

    /**
     * Test for concrete domains taken from the paper "Tractable Extensions of
     * the Description Logic EL with Numerical Datatypes". It uses integer
     * values and the operators less than, equals, and greater than.
     */
    //@Test
    public void testConcreteDomainsOperators() {
        IFactory factory = new CoreFactory();

        // Add roles
        Role contains = new Role("contains");
        Role hasPrescription = new Role("hasPrescription");

        // Add features
        Feature mgPerTablet = new Feature("mgPerTablet");
        Feature hasAge = new Feature("hasAge");

        // Add concepts
        Concept panadol = new Concept("Panadol");
        Concept paracetamol = new Concept("Paracetamol");
        Concept patient = new Concept("Patient");
        Concept X = new Concept("X");

        // Add axioms
        ConceptInclusion a1 = new ConceptInclusion(panadol, 
                new Existential(contains, 
                        new Conjunction(new IConcept[] {
                        paracetamol,
                        new Datatype(mgPerTablet, Operator.EQUALS,
                                new IntegerLiteral(500)) })));

        ConceptInclusion a2 = new ConceptInclusion(
                new Conjunction(new IConcept[] {
                        patient,
                        new Datatype(hasAge, Operator.LESS_THAN,
                                new IntegerLiteral(6)),
                        new Existential(hasPrescription, 
                                new Existential(contains,
                                new Conjunction(new IConcept[] {
                                        paracetamol,
                                        new Datatype(mgPerTablet,
                                                Operator.GREATER_THAN,
                                                new IntegerLiteral(250)) }))) }),
                Concept.BOTTOM_CONCEPT);

        ConceptInclusion a3 = new ConceptInclusion(X, new Conjunction(
                new IConcept[] {
                        patient,
                        new Datatype(hasAge, Operator.EQUALS,
                                new IntegerLiteral(3)),
                        new Existential(hasPrescription, panadol) }));

        Set<IAxiom> axioms = new HashSet<IAxiom>();
        axioms.add(a1);
        axioms.add(a2);
        axioms.add(a3);

        // Classify
        NormalisedOntology o = 
                new NormalisedOntology(factory, axioms);
        o.classify();
        
        // Build taxonomy
        o.buildTaxonomy();

        // Test results
        Node panadolNode = o.getEquivalents(panadol.getId());
        Set<Node> panadolRes = panadolNode.getParents();
        assertTrue(panadolRes.size() == 1);
        assertTrue(panadolRes
                .contains(o.getTopNode()));

        Node paracetamolNode = o.getEquivalents(paracetamol.getId());
        Set<Node> paracetamolRes = paracetamolNode.getParents();
        assertTrue(paracetamolRes.size() == 1);
        assertTrue(paracetamolRes.contains(o.getTopNode()));

        Node patientNode = o.getEquivalents(patient.getId());
        Set<Node> patientRes = patientNode.getParents();
        assertTrue(patientRes.size() == 1);
        assertTrue(patientRes.contains(o.getTopNode()));

        Node bottomNode = o.getBottomNode();
        assertTrue(bottomNode.getEquivalentConcepts().size() == 2);
        assertTrue(bottomNode.getEquivalentConcepts().contains(X.getId()));
        Set<Node> bottomRes = bottomNode.getParents();
        assertTrue(bottomRes.size() == 3);
        assertTrue(bottomRes.contains(o.getEquivalents(panadol.getId())));
        assertTrue(bottomRes.contains(o.getEquivalents(paracetamol.getId())));
        assertTrue(bottomRes.contains(o.getEquivalents(patient.getId())));
    }
    
    @Test
    public void testConcreteDomainsInequality() {
        IFactory factory = new CoreFactory();

        // Add features
        Feature strength = new Feature("strength");

        // Add concepts
        Concept cat10 = new Concept("CAT10");
        Concept catLt10 = new Concept("CAT_LT10");

        // Add axioms
        ConceptInclusion ax1 = new ConceptInclusion(cat10, 
                new Datatype(strength, Operator.EQUALS, 
                new FloatLiteral(10)));
        
        ConceptInclusion ax1b = new ConceptInclusion( 
                new Datatype(strength, Operator.EQUALS, 
                new FloatLiteral(10)), cat10);
        
        ConceptInclusion ax2 = new ConceptInclusion(catLt10, 
                new Datatype(strength, Operator.LESS_THAN, 
                new FloatLiteral(10)));
        
        ConceptInclusion ax2b = new ConceptInclusion( 
                new Datatype(strength, Operator.LESS_THAN, 
                new FloatLiteral(10)), catLt10);

        

        Set<IAxiom> axioms = new HashSet<IAxiom>();
        axioms.add(ax1);
        axioms.add(ax1b);
        axioms.add(ax2);
        axioms.add(ax2b);

        // Classify
        NormalisedOntology o = 
                new NormalisedOntology(factory, axioms);
        o.classify();
        
        // Build taxonomy
        o.buildTaxonomy();

        // Test results
        Node cat10Node = o.getEquivalents(cat10.getId());
        Set<Node> cat10Children = cat10Node.getChildren();
        Assert.assertEquals(1, cat10Children.size());
        Node cat10Child = cat10Children.iterator().next();
        Assert.assertEquals(o.getBottomNode(), cat10Child);

        Node catLt10Node = o.getEquivalents(catLt10.getId());
        Set<Node> catLt10Children = catLt10Node.getChildren();
        Assert.assertEquals(1, catLt10Children.size());
        Node catLt10Child = catLt10Children.iterator().next();
        Assert.assertEquals(o.getBottomNode(), catLt10Child);
    }

}
