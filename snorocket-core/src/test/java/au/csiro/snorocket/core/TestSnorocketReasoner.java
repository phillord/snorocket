/**
 * Copyright CSIRO Australian e-Health Research Centre (http://aehrc.com).
 * All rights reserved. Use is subject to license terms and conditions.
 */
package au.csiro.snorocket.core;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Test;

import au.csiro.ontology.Factory;
import au.csiro.ontology.IOntology;
import au.csiro.ontology.Node;
import au.csiro.ontology.axioms.ConceptInclusion;
import au.csiro.ontology.axioms.IAxiom;
import au.csiro.ontology.axioms.RoleInclusion;
import au.csiro.ontology.model.Concept;
import au.csiro.ontology.model.Conjunction;
import au.csiro.ontology.model.Existential;
import au.csiro.ontology.model.IConcept;
import au.csiro.ontology.model.Role;
import au.csiro.snorocket.core.util.Utils;

/**
 * @author Alejandro Metke
 *
 */
public class TestSnorocketReasoner {
    
    /**
     * 
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testSave() {

        // Original Endocarditis ontology axioms
        Role<String> contIn = new Role<String>("cont-in");
        Role<String> partOf = new Role<String>("part-of");
        Role<String> hasLoc = new Role<String>("has-loc");
        Role<String> actsOn = new Role<String>("acts-on");
        Concept<String> tissue = new Concept<String>("Tissue");
        Concept<String> heartWall = new Concept<String>("HeartWall");
        Concept<String> heartValve = new Concept<String>("HeartValve");
        Concept<String> bodyWall = new Concept<String>("BodyWall");
        Concept<String> heart = new Concept<String>("Heart");
        Concept<String> bodyValve = new Concept<String>("BodyValve");
        Concept<String> inflammation = new Concept<String>("Inflammation");
        Concept<String> disease = new Concept<String>("Disease");
        Concept<String> heartdisease = new Concept<String>("Heartdisease");
        Concept<String> criticalDisease = new Concept<String>("CriticalDisease");

        ConceptInclusion a2 = new ConceptInclusion(heartWall, new Conjunction(
                new IConcept[] { bodyWall,
                        new Existential<String>(partOf, heart) }));

        ConceptInclusion a3 = new ConceptInclusion(heartValve, new Conjunction(
                new IConcept[] { bodyValve,
                        new Existential<String>(partOf, heart) }));

        ConceptInclusion a5 = new ConceptInclusion(inflammation,
                new Conjunction(new IConcept[] { disease,
                        new Existential<String>(actsOn, tissue) }));

        ConceptInclusion a6 = new ConceptInclusion(new Conjunction(
                new IConcept[] { heartdisease,
                        new Existential<String>(hasLoc, heartValve) }), criticalDisease);

        ConceptInclusion a7 = new ConceptInclusion(heartdisease,
                new Conjunction(new IConcept[] { disease,
                        new Existential<String>(hasLoc, heart) }));

        ConceptInclusion a8 = new ConceptInclusion(
                new Conjunction(new IConcept[] { disease,
                        new Existential<String>(hasLoc, heart) }), heartdisease);

        RoleInclusion a9 = new RoleInclusion(new Role[] { partOf, partOf },
                partOf);
        RoleInclusion a10 = new RoleInclusion(partOf, contIn);
        RoleInclusion a11 = new RoleInclusion(new Role[] { hasLoc, contIn },
                hasLoc);

        // Partial ontology
        Set<IAxiom> axioms = new HashSet<IAxiom>();
        axioms.add(a2);
        axioms.add(a3);
        axioms.add(a5);
        axioms.add(a6);
        axioms.add(a7);
        axioms.add(a8);
        axioms.add(a9);
        axioms.add(a10);
        axioms.add(a11);
        
        SnorocketReasoner<String> sr = new SnorocketReasoner<String>();
        sr.classify(axioms);
        
        try {
            // Save to temp file
            File temp = File.createTempFile("temp",".ser");
            temp.deleteOnExit();
            sr.save(new FileOutputStream(temp));
            
            sr = null;
            sr = SnorocketReasoner.load(new FileInputStream(temp));
        } catch(Exception ex) {
            ex.printStackTrace();
            assertTrue(false);
        }

        // Add delta axioms and classify incrementally
        Concept<String> endocardium = new Concept<String>("Endocardium");
        Concept<String> endocarditis = new Concept<String>("Endocarditis");

        ConceptInclusion a1 = new ConceptInclusion(endocardium,
                new Conjunction(new IConcept[] { tissue,
                        new Existential<String>(contIn, heartWall),
                        new Existential<String>(contIn, heartValve) }));

        ConceptInclusion a4 = new ConceptInclusion(endocarditis,
                new Conjunction(new IConcept[] { inflammation,
                        new Existential<String>(hasLoc, endocardium) }));

        Set<IAxiom> incAxioms = new HashSet<IAxiom>();
        incAxioms.add(a1);
        incAxioms.add(a4);

        sr.classify(incAxioms);

        // Test results
        IOntology<String> ont = sr.getClassifiedOntology();
        
        Node<String> bottom = ont.getBottomNode();
        Set<Node<String>> bottomRes = bottom.getParents();
        assertTrue(bottomRes.size() == 5);
        assertTrue(bottomRes.contains(ont.getNode(endocardium.getId())));
        assertTrue(bottomRes.contains(ont.getNode(endocarditis.getId())));
        assertTrue(bottomRes.contains(ont.getNode(heartWall.getId())));
        assertTrue(bottomRes.contains(ont.getNode(heartValve.getId())));
        assertTrue(bottomRes.contains(ont.getNode(heart.getId())));

        Node<String> endocarditisNode = ont.getNode(endocarditis.getId());
        Set<Node<String>> endocarditisRes = endocarditisNode.getParents();
        assertTrue(endocarditisRes.size() == 3);
        assertTrue(endocarditisRes.contains(ont.getNode(inflammation.getId())));
        assertTrue(endocarditisRes.contains(ont.getNode(heartdisease.getId())));
        assertTrue(endocarditisRes.contains(ont.getNode(criticalDisease.getId())));

        Node<String> inflammationNode = ont.getNode(inflammation.getId());
        Set<Node<String>> inflammationRes = inflammationNode.getParents();
        assertTrue(inflammationRes.size() == 1);
        assertTrue(inflammationRes.contains(ont.getNode(disease.getId())));

        Node<String> endocardiumNode = ont.getNode(endocardium.getId());
        Set<Node<String>> endocardiumRes = endocardiumNode.getParents();
        assertTrue(endocardiumRes.size() == 1);
        assertTrue(endocardiumRes.contains(ont.getNode(tissue.getId())));

        Node<String> heartdiseaseNode = ont.getNode(heartdisease.getId());
        Set<Node<String>> heartdiseaseRes = heartdiseaseNode.getParents();
        assertTrue(heartdiseaseRes.size() == 1);
        assertTrue(heartdiseaseRes.contains(ont.getNode(disease.getId())));

        Node<String> heartWallNode = ont.getNode(heartWall.getId());
        Set<Node<String>> heartWallRes = heartWallNode.getParents();
        assertTrue(heartWallRes.size() == 1);
        assertTrue(heartWallRes.contains(ont.getNode(bodyWall.getId())));

        Node<String> heartValveNode = ont.getNode(heartValve.getId());
        Set<Node<String>> heartValveRes = heartValveNode.getParents();
        assertTrue(heartValveRes.size() == 1);
        assertTrue(heartValveRes
                .contains(ont.getNode(bodyValve.getId())));

        Node<String> diseaseNode = ont.getNode(disease.getId());
        Set<Node<String>> diseaseRes = diseaseNode.getParents();
        assertTrue(diseaseRes.size() == 1);
        assertTrue(diseaseRes.contains(ont.getTopNode()));

        Node<String> tissueNode = ont.getNode(tissue.getId());
        Set<Node<String>> tissueRes = tissueNode.getParents();
        assertTrue(tissueRes.size() == 1);
        assertTrue(tissueRes.contains(ont.getTopNode()));

        Node<String> heartNode = ont.getNode(heart.getId());
        Set<Node<String>> heartRes = heartNode.getParents();
        assertTrue(heartRes.size() == 1);
        assertTrue(heartRes.contains(ont.getTopNode()));

        Node<String> bodyValveNode = ont.getNode(bodyValve.getId());
        Set<Node<String>> bodyValveRes = bodyValveNode.getParents();
        assertTrue(bodyValveRes.size() == 1);
        assertTrue(bodyValveRes.contains(ont.getTopNode()));

        Node<String> bodyWallNode = ont.getNode(bodyWall.getId());
        Set<Node<String>> bodyWallRes = bodyWallNode.getParents();
        assertTrue(bodyWallRes.size() == 1);
        assertTrue(bodyWallRes.contains(ont.getTopNode()));

        Node<String> criticalDiseaseNode = ont.getNode(criticalDisease.getId());
        Set<Node<String>> criticalDiseaseRes = criticalDiseaseNode.getParents();
        assertTrue(criticalDiseaseRes.size() == 1);
        assertTrue(criticalDiseaseRes.contains(ont.getTopNode()));
    }
    
    @Test
    public void testNesting() {
        Role<String> rg = new Role<String>("RoleGroup");
        Role<String> fs = new Role<String>("site");
        Role<String> am = new Role<String>("morph");
        Role<String> lat = new Role<String>("lat");
        
        Concept<String> finding = new Concept<String>("Finding");
        Concept<String> fracfind = new Concept<String>("FractureFinding");
        Concept<String> limb = new Concept<String>("Limb");
        Concept<String> arm = new Concept<String>("Arm");
        Concept<String> left = new Concept<String>("Left");
        Concept<String> fracture = new Concept<String>("Fracture");
        Concept<String> burn = new Concept<String>("Burn");
        Concept<String> right = new Concept<String>("Right");
        Concept<String> multi = new Concept<String>("Multiple");
        
        IConcept[] larm = {
                arm, new Existential<String>(lat, left)
        };
        IConcept[] rarm = {
                arm, new Existential<String>(lat, right)
        };
        IConcept[] g1 = {
                new Existential<String>(fs, new Conjunction(rarm)),
                new Existential<String>(fs, arm),
                new Existential<String>(am, fracture),
        };
        IConcept[] g2 = {
                new Existential<String>(fs, new Conjunction(larm)),
                new Existential<String>(am, burn),
        };
        IConcept[] rhs = {
                finding,
                new Existential<String>(rg, new Conjunction(g1)),
                new Existential<String>(rg, new Conjunction(g2)),
        };
        IConcept[] rhs2 = {
                finding,
                new Existential<String>(rg, new Existential<String>(am, fracture)),
        };
        IAxiom[] inclusions = {
                new ConceptInclusion(multi, new Conjunction(rhs)),
                new ConceptInclusion(arm, limb),
                new ConceptInclusion(fracfind, new Conjunction(rhs2)),
                new ConceptInclusion(new Conjunction(rhs2), fracfind),
        };
        
        Set<IAxiom> axioms = new HashSet<IAxiom>();
        for (IAxiom a : inclusions) {
            axioms.add(a);
        }

        // Classify
        SnorocketReasoner<String> sr = new SnorocketReasoner<String>();
        sr.classify(axioms);
        
        IOntology<String> ont = sr.getClassifiedOntology();
        
        Utils.printTaxonomy(ont.getTopNode(), ont.getBottomNode());
        
        try {
            for (IAxiom a: axioms) {
                System.out.println("Stated: " + a);
            }
            for (IAxiom a: sr.getInferredAxioms()) {
                System.out.println("Axiom:  " + a);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    @Test
    public void testEndocarditis() {
        org.apache.log4j.LogManager.getRootLogger().setLevel((org.apache.log4j.Level)org.apache.log4j.Level.TRACE);
        // Create roles
        Role<String> contIn = new Role<String>("cont-in");
        Role<String> partOf = new Role<String>("part-of");
        Role<String> hasLoc = new Role<String>("has-loc");
        Role<String> actsOn = new Role<String>("acts-on");

        // Create concepts
        Concept<String> endocardium = new Concept<String>("Endocardium");
        Concept<String> tissue = new Concept<String>("Tissue");
        Concept<String> heartWall = new Concept<String>("HeartWall");
        Concept<String> heartValve = new Concept<String>("HeartValve");
        Concept<String> bodyWall = new Concept<String>("BodyWall");
        Concept<String> heart = new Concept<String>("Heart");
        Concept<String> bodyValve = new Concept<String>("BodyValve");
        Concept<String> endocarditis = new Concept<String>("Endocarditis");
        Concept<String> inflammation = new Concept<String>("Inflammation");
        Concept<String> disease = new Concept<String>("Disease");
        Concept<String> heartdisease = new Concept<String>("Heartdisease");
        Concept<String> criticalDisease = new Concept<String>("CriticalDisease");

        // Create axioms
        ConceptInclusion a1 = new ConceptInclusion(endocardium,
                new Conjunction(new IConcept[] { tissue,
                        new Existential<String>(contIn, heartWall),
                        new Existential<String>(contIn, heartValve) }));

        ConceptInclusion a2 = new ConceptInclusion(heartWall, new Conjunction(
                new IConcept[] { bodyWall,
                        new Existential<String>(partOf, heart) }));

        ConceptInclusion a3 = new ConceptInclusion(heartValve, new Conjunction(
                new IConcept[] { bodyValve,
                        new Existential<String>(partOf, heart) }));

        ConceptInclusion a4 = new ConceptInclusion(endocarditis,
                new Conjunction(new IConcept[] { inflammation,
                        new Existential<String>(hasLoc, endocardium) }));

        ConceptInclusion a5 = new ConceptInclusion(inflammation,
                new Conjunction(new IConcept[] { disease,
                        new Existential<String>(actsOn, tissue) }));

        ConceptInclusion a6 = new ConceptInclusion(new Conjunction(
                new IConcept[] { heartdisease,
                        new Existential<String>(hasLoc, heartValve) }), 
                        criticalDisease);

        ConceptInclusion a7 = new ConceptInclusion(heartdisease,
                new Conjunction(new IConcept[] { disease,
                        new Existential<String>(hasLoc, heart) }));

        ConceptInclusion a8 = new ConceptInclusion(
                new Conjunction(new IConcept[] { disease,
                        new Existential<String>(hasLoc, heart) }), 
                        heartdisease);

        RoleInclusion a9 = new RoleInclusion(new Role[] { partOf, partOf },
                partOf);
        RoleInclusion a10 = new RoleInclusion(partOf, contIn);
        RoleInclusion a11 = new RoleInclusion(new Role[] { hasLoc, contIn },
                hasLoc);

        Set<IAxiom> axioms = new HashSet<IAxiom>();
        axioms.add(a1);
        axioms.add(a2);
        axioms.add(a3);
        axioms.add(a4);
        axioms.add(a5);
        axioms.add(a6);
        axioms.add(a7);
        axioms.add(a8);
        axioms.add(a9);
        axioms.add(a10);
        axioms.add(a11);

        // Classify
        SnorocketReasoner<String> sr = new SnorocketReasoner<String>();
        sr.classify(axioms);
        
        IOntology<String> ont = sr.getClassifiedOntology();
        
        Utils.printTaxonomy(ont.getTopNode(), ont.getBottomNode());
        
        // Test taxonomy results
        Node<String> bottomNode = ont.getBottomNode();
        Set<Node<String>> bottomRes = bottomNode.getParents();

        assertTrue(bottomRes.size() == 5);
        assertTrue(bottomRes.contains(ont.getNode(endocardium.getId())));
        assertTrue(bottomRes.contains(ont.getNode(endocarditis.getId())));
        assertTrue(bottomRes.contains(ont.getNode(heartWall.getId())));
        assertTrue(bottomRes.contains(ont.getNode(heartValve.getId())));
        assertTrue(bottomRes.contains(ont.getNode(heart.getId())));

        Node<String> endocarditisNode = ont.getNode(endocarditis.getId());
        Set<Node<String>> endocarditisRes = endocarditisNode.getParents();
        assertTrue(endocarditisRes.size() == 3);
        assertTrue(endocarditisRes.contains(ont.getNode(inflammation.getId())));
        assertTrue(endocarditisRes.contains(ont.getNode(heartdisease.getId())));
        assertTrue(endocarditisRes.contains(ont.getNode(criticalDisease.getId())));

        Node<String> inflammationNode = ont.getNode(inflammation.getId());
        Set<Node<String>> inflammationRes = inflammationNode.getParents();
        assertTrue(inflammationRes.size() == 1);
        assertTrue(inflammationRes.contains(ont.getNode(disease.getId())));

        Node<String> endocardiumNode = ont.getNode(endocardium.getId());
        Set<Node<String>> endocardiumRes = endocardiumNode.getParents();
        assertTrue(endocardiumRes.size() == 1);
        assertTrue(endocardiumRes.contains(ont.getNode(tissue.getId())));

        Node<String> heartdiseaseNode = ont.getNode(heartdisease.getId());
        Set<Node<String>> heartdiseaseRes = heartdiseaseNode.getParents();
        assertTrue(heartdiseaseRes.size() == 1);
        assertTrue(heartdiseaseRes.contains(ont.getNode(disease.getId())));

        Node<String> heartWallNode = ont.getNode(heartWall.getId());
        Set<Node<String>> heartWallRes = heartWallNode.getParents();
        assertTrue(heartWallRes.size() == 1);
        assertTrue(heartWallRes.contains(ont.getNode(bodyWall.getId())));

        Node<String> heartValveNode = ont.getNode(heartValve.getId());
        Set<Node<String>> heartValveRes = heartValveNode.getParents();
        assertTrue(heartValveRes.size() == 1);
        assertTrue(heartValveRes.contains(ont.getNode(bodyValve.getId())));

        Node<String> diseaseNode = ont.getNode(disease.getId());
        Set<Node<String>> diseaseRes = diseaseNode.getParents();
        assertTrue(diseaseRes.size() == 1);
        assertTrue(diseaseRes.contains(ont.getTopNode()));

        Node<String> tissueNode = ont.getNode(tissue.getId());
        Set<Node<String>> tissueRes = tissueNode.getParents();
        assertTrue(tissueRes.size() == 1);
        assertTrue(tissueRes.contains(ont.getTopNode()));

        Node<String> heartNode = ont.getNode(heart.getId());
        Set<Node<String>> heartRes = heartNode.getParents();
        assertTrue(heartRes.size() == 1);
        assertTrue(heartRes.contains(ont.getTopNode()));

        Node<String> bodyValveNode = ont.getNode(bodyValve.getId());
        Set<Node<String>> bodyValveRes = bodyValveNode.getParents();
        assertTrue(bodyValveRes.size() == 1);
        assertTrue(bodyValveRes.contains(ont.getTopNode()));

        Node<String> bodyWallNode = ont.getNode(bodyWall.getId());
        Set<Node<String>> bodyWallRes = bodyWallNode.getParents();
        assertTrue(bodyWallRes.size() == 1);
        assertTrue(bodyWallRes.contains(ont.getTopNode()));

        Node<String> criticalDiseaseNode = ont.getNode(criticalDisease.getId());
        Set<Node<String>> criticalDiseaseRes = criticalDiseaseNode.getParents();
        assertTrue(criticalDiseaseRes.size() == 1);
        assertTrue(criticalDiseaseRes.contains(ont.getTopNode()));
        
        try {
            for (IAxiom a: sr.getInferredAxioms()) {
                System.out.println("Axiom: " + a);
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
    
    /**
     * Tests the identification of possibly affected concepts after an
     * incremental taxonomy calculation.
     */
    @Test
    public void testIncrementalTaxonomy() {
    	
    	Factory<String> fac = new Factory<String>();
    	IConcept a = fac.createConcept("A");
    	IConcept b = fac.createConcept("B");
    	IConcept c = fac.createConcept("C");
    	IConcept d = fac.createConcept("D");
    	IConcept e = fac.createConcept("E");
    	IConcept f = fac.createConcept("F");
    	IConcept g = fac.createConcept("G");
    	
    	IAxiom a1 = fac.createConceptInclusion(b,  a);
    	IAxiom a2 = fac.createConceptInclusion(c,  b);
    	IAxiom a3 = fac.createConceptInclusion(d,  c);
    	IAxiom a4 = fac.createConceptInclusion(e,  a);
    	IAxiom a5 = fac.createConceptInclusion(f,  e);
    	
    	Set<IAxiom> axioms = new HashSet<IAxiom>();
        axioms.add(a1);
        axioms.add(a2);
        axioms.add(a3);
        axioms.add(a4);
        axioms.add(a5);
    	
    	SnorocketReasoner<String> sr = new SnorocketReasoner<String>();
        sr.classify(axioms);
        
        IOntology<String> ont = sr.getClassifiedOntology();
        Utils.printTaxonomy(ont.getTopNode(), ont.getBottomNode());
        
        IAxiom a6 = fac.createConceptInclusion(g,  e);
        IAxiom a7 = fac.createConceptInclusion(f,  g);
        
        axioms.clear();
        axioms.add(a6);
        axioms.add(a7);
        
        sr.classify(axioms);
        ont = sr.getClassifiedOntology();
        
        Utils.printTaxonomy(ont.getTopNode(), ont.getBottomNode());
        
        Set<Node<String>> affectedNodes = ont.getAffectedNodes();
        Set<String> affectedIds = new HashSet<String>();
        for(Node<String> affectedNode : affectedNodes) {
        	affectedIds.addAll(affectedNode.getEquivalentConcepts());
        }
        
        System.out.println("Affected node ids: "+affectedIds);
        
        Assert.assertTrue("Node G was not found in affected nodes", 
        		affectedIds.contains("G"));
        
        Assert.assertTrue("Node F was not found in affected nodes", 
        		affectedIds.contains("F"));
    }

}
