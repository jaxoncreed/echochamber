package team.o.echochamber;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.swrlapi.core.SWRLRuleEngine;
import org.swrlapi.factory.SWRLAPIFactory;
import org.swrlapi.sqwrl.SQWRLQueryEngine;
import org.swrlapi.sqwrl.SQWRLResult;

import java.io.File;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

public class EchochamberDatastore {
    private OWLOntologyManager m;
    private OWLDataFactory f;
    private OWLOntology ontology;
    private SWRLRuleEngine ruleEngine;
    private SQWRLQueryEngine queryEngine;

    private String base;
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

    private List<String> rules;

    private void init() {
        base = ontology.getOntologyID().getOntologyIRI().get().toString() + "#";
        m.getOntologyFormat(ontology).asPrefixOWLOntologyFormat().setDefaultPrefix(base);
        m.getOntologyFormat(ontology).asPrefixOWLOntologyFormat().setPrefix("ec", ec);

        rules = new ArrayList<>();

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

        m.applyChanges(ecChanges);

        ruleEngine = SWRLAPIFactory.createSWRLRuleEngine(ontology);
        queryEngine = SWRLAPIFactory.createSQWRLQueryEngine(ontology);

        ruleEngine.infer();
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
        rules.add(rule);
    }

    /**
     * Add a triple to the datastore
     * @param subject
     * @param predicate
     * @param object
     */
    public void addTriple(String subject, String predicate, String object) {
        // Initialize the variables for needed axioms
        UUID uuid = UUID.randomUUID();
        OWLIndividual action = f.getOWLNamedIndividual(IRI.create(ec + "action" + uuid));
        OWLIndividual triple = f.getOWLNamedIndividual(IRI.create(ec + "triple1" + uuid));
        OWLIndividual Subject = f.getOWLNamedIndividual(IRI.create(base + subject));
        OWLDataProperty Predicate = f.getOWLDataProperty(IRI.create(base + predicate));
        OWLIndividual PredicateIndividual = f.getOWLNamedIndividual(IRI.create(base + predicate));

        // Add the triple
        ArrayList<OWLOntologyChange> changes = new ArrayList<>();
        changes.add(new AddAxiom(ontology, f.getOWLDataPropertyAssertionAxiom(Predicate, Subject, object)));

        // Add the action representing the triple
        changes.add(new AddAxiom(ontology, f.getOWLClassAssertionAxiom(AddedAction, action)));
        changes.add(new AddAxiom(ontology, f.getOWLObjectPropertyAssertionAxiom(actionTriple, action, triple)));
        changes.add(new AddAxiom(ontology, f.getOWLObjectPropertyAssertionAxiom(s, triple, Subject)));
        changes.add(new AddAxiom(ontology, f.getOWLObjectPropertyAssertionAxiom(p, triple, PredicateIndividual)));
        changes.add(new AddAxiom(ontology, f.getOWLDataPropertyAssertionAxiom(o, triple, object)));

        m.applyChanges(changes);

        printOntology();

        // Run sqwrl query
        List<String> routesToQuery = new ArrayList<>();
        for (String rule : rules) {
            try {
                SQWRLResult result = queryEngine.runSQWRLQuery("q" + uuid, rule);
                if (result.next()) {
                    routesToQuery.addAll(
                        Arrays.asList(result.getColumn("webhook").toArray())
                                .stream().map(i -> i.toString()).collect(Collectors.toList())
                    );
                    result.getColumn("webhook").toArray();
                }
            } catch(Exception e) {
                System.err.println(e);
            }

        }
        for (String url : routesToQuery) {
            callWebhook(URI.create(url.substring(1, url.length() - 13)));
        }

    }

    private void callWebhook(URI url) {
        // Perform actual query here
        System.out.println(url);
    }

}
