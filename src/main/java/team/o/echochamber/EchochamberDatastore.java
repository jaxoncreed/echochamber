package team.o.echochamber;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.sqwrl.SQWRLResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Optional;

public class EchochamberDatastore {
    private OWLOntologyManager m;
    private OWLDataFactory f;
    private OWLOntology ontology;
    private SWRLRuleEngine ruleEngine;

    private String ec = "http://echachamber.o.team#";

    private OWLClass Action;
    private OWLClass AddedAction;
    private OWLClass Triple;
    private OWLObjectProperty chatWebhook;
    private OWLObjectProperty actionType;
    private OWLObjectProperty actionTriple;
    private OWLObjectProperty s;
    private OWLObjectProperty p;
    private OWLDataProperty o;
    private OWLObjectProperty shouldUpdate;

    private void init() {
        m.getOntologyFormat(ontology).asPrefixOWLOntologyFormat().setDefaultPrefix(ontology.getOntologyID().getOntologyIRI().toString() + "#");
        m.getOntologyFormat(ontology).asPrefixOWLOntologyFormat().setPrefix("ec", ec);

        Action = f.getOWLClass(IRI.create(ec + "Action"));
        AddedAction = f.getOWLClass(IRI.create(ec + "AddedAction"));
        Triple = f.getOWLClass(IRI.create(ec + "Triple"));
        chatWebhook = f.getOWLObjectProperty(IRI.create(ec + "chatWebhook"));
        actionType = f.getOWLObjectProperty(IRI.create(ec + "actionType"));
        actionTriple = f.getOWLObjectProperty(IRI.create(ec + "actionTriple"));
        s = f.getOWLObjectProperty(IRI.create(ec + "s"));
        p = f.getOWLObjectProperty(IRI.create(ec + "p"));
        o = f.getOWLDataProperty(IRI.create(ec + "o"));
        shouldUpdate = f.getOWLObjectProperty(IRI.create(ec + "shouldUpdate"));

        ArrayList<OWLOntologyChange> ecChanges = new ArrayList<>();

        ecChanges.add(new AddAxiom(ontology, f.getOWLDeclarationAxiom(Action)));
        ecChanges.add(new AddAxiom(ontology, f.getOWLDeclarationAxiom(AddedAction)));
        ecChanges.add(new AddAxiom(ontology, f.getOWLDeclarationAxiom(Triple)));
        ecChanges.add(new AddAxiom(ontology, f.getOWLSubClassOfAxiom(AddedAction, Action)));
        ecChanges.add(new AddAxiom(ontology, f.getOWLDeclarationAxiom(chatWebhook)));
        ecChanges.add(new AddAxiom(ontology, f.getOWLDeclarationAxiom(actionType)));
        ecChanges.add(new AddAxiom(ontology, f.getOWLDeclarationAxiom(actionTriple)));
        ecChanges.add(new AddAxiom(ontology, f.getOWLDeclarationAxiom(s)));
        ecChanges.add(new AddAxiom(ontology, f.getOWLDeclarationAxiom(p)));
        ecChanges.add(new AddAxiom(ontology, f.getOWLDeclarationAxiom(o)));
        ecChanges.add(new AddAxiom(ontology, f.getOWLDeclarationAxiom(shouldUpdate)));

        System.out.println("before apply change");

        m.applyChanges(ecChanges);

        ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(ontology);

        System.out.println("after define rule engine");

        ruleEngine.infer();

        System.out.println("after infer");

        printOntology();
    }

    private void printOntology() {
        try {
            m.saveOntology(ontology, System.out);
        } catch(OWLOntologyStorageException e) {
            System.err.println(e);
        }
    }

    /**
     * Initialize datastore
     */
    public EchochamberDatastore() throws OWLOntologyCreationException {
        m = OWLManager.createOWLOntologyManager();
        f = m.getOWLDataFactory();
        ontology = m.createOntology();
        init();
    }

    /**
     * Initialize datastore
     * @param filename the name of an OWL file to import
     */
    public EchochamberDatastore(String filename) throws OWLOntologyCreationException {
        m = OWLManager.createOWLOntologyManager();
        f = m.getOWLDataFactory();
        ontology = m.loadOntologyFromOntologyDocument(new File(filename));
        init();
    }

    /**
     * Add a swrl rule to the datastore
     * @param rule a SWRL rule
     */
    public void addRule(String rule) {

    }

    /**
     * Add a triple to the datastore
     * @param subject
     * @param predicate
     * @param object
     */
    public void addTriple(String subject, String predicate, String object) {

    }

}
