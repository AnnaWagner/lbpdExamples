
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.ReasonerFactory;
import org.apache.jena.reasoner.ReasonerRegistry;

import java.util.ArrayList;
import java.util.HashMap;

public class main {
    private static Model model;
    private static Model bpo;
    private static String bpoLocation="C:\\Users\\Anna Wagner\\Documents\\02_myOntologies\\bpo\\bpo\\bpo.ttl", base;
    private static HashMap<String,String> products;


    public static void main (String[] args){
        bpo = ModelFactory.createDefaultModel();
        bpo.read(bpoLocation);

        init();

        for(String p : products.keySet()){
            System.out.println("Reading file: "+p);
            readModel(base + p + ".ttl");
            getQuantity(products.get(p));
        }

    }

    private static void init(){
        base = "C:\\Users\\Anna Wagner\\Documents\\00_Diss\\examples\\lbpdExamples\\freedomOfModelling\\";
        products = new HashMap<>();
        products.put("PVShade_V1","3vHNbSoT0Hsm00051Mm008");
        products.put("PVShade_V2","3vHNbSoT0Hsm00051Mm008");
        products.put("PVShade_V3","3vHNbSoT0Hsm00051Mm008");
        products.put("GebaeudeN_V1","3vHNbSoT0Hsm00051Mm008");
        products.put("GebaeudeN_V2","3vHNbSoT0Hsm00051Mm008");
        products.put("GebaeudeN_V3","3vHNbSoT0Hsm00051Mm008");
        products.put("CostEffective_V1","scp051");
        products.put("CostEffective_V2","scp051");
        products.put("CostEffective_V3","scp051");
    }

    private static void getQuantity(String bsdd){
        int quantity=0;
        ArrayList<String> elements = getElementURI(bsdd);
        for(String e : elements) {
            quantity += getQuantityOfElementEntity(e) * getAssemblyAndQuantity(e);
        }
        System.out.println("Quantity of entities: "+quantity);
    }

    private static int getAssemblyAndQuantity(String elementURI){
        int quantity = 1 ;
        ArrayList<String> assemblies = getAssemblyURI(elementURI);
        for(String a : assemblies)
            quantity *= getQuantityOfElementEntity(a) * getAssemblyAndQuantity(a);
        return quantity;
    }

    private static void readModel(String file){
        model = ModelFactory.createDefaultModel();
        model.read(file);
//        System.out.println(localModel.size());
//        model = ModelFactory.createInfModel(ReasonerRegistry.getRDFSReasoner(),bpo,localModel);
//        System.out.println(model.size());
    }

    private static ArrayList<String> getElementURI(String bsdd){
        ArrayList<String> eURI=new ArrayList<>();
//        System.out.println(model.size());
        String query= "PREFIX bpo: <https://w3id.org/bpo#> \n" +
                "SELECT ?element\n" +
                "WHERE{\n" +
                "\t?element bpo:hasBSDDGUID '" + bsdd +"' ." +
                "}";
//        System.out.println(query);
        Query sparql = QueryFactory.create(query);
        QueryExecution qexec = QueryExecutionFactory.create(sparql,model);
        ResultSet result = qexec.execSelect();
        while(result.hasNext()){
//            System.out.println("read element uri");
            eURI.add(result.next().get("?element").asResource().getURI());
        }
        return eURI;
    }

    private static ArrayList<String> getAssemblyURI(String elementURI){
        ArrayList<String> aURI=new ArrayList<>();
        String query= "PREFIX bpo: <https://w3id.org/bpo#> \n" +
                "SELECT DISTINCT ?assembly\n" +
                "WHERE{\n" +
                "\t?assembly bpo:isComposedOfEntity/bpo:realisesObject <" + elementURI + "> . \n" +
                "\tFILTER NOT EXISTS {?assembly a bpo:Product }" +
                "}";
//        System.out.println(query);
        Query sparql = QueryFactory.create(query);
        QueryExecution qexec = QueryExecutionFactory.create(sparql,model);
        ResultSet result = qexec.execSelect();
        while(result.hasNext()){
//            System.out.println("read assembly uri");
            aURI.add(result.next().get("?assembly").asResource().getURI());
        }
        return aURI;
    }

    private static int getQuantityOfElementEntity(String elementURI){
        int quantity=0;
        String query= "PREFIX bpo: <https://w3id.org/bpo#> \n" +
                "SELECT ?number\n" +
                "WHERE{\n" +
                "\t{\n" +
                "\t\tSELECT (COUNT(?ent) AS ?number)\n" +
                "\t\tWHERE {\n" +
                "\t\t\t?ent bpo:realisesObject <"+elementURI+"> ;\n" +
                "\t\t\t\ta bpo:SingularEntity .\n" +
                "\t\t}\n" +
                "\t} UNION {\n" +
                "\t\t?ent bpo:realisesObject <"+elementURI+"> ;\n" +
                "\t\t\tbpo:hasQuantity ?number .\n" +
                "\t}\n" +
                "}";
//        System.out.println(query);
        Query sparql = QueryFactory.create(query);
        QueryExecution qexec = QueryExecutionFactory.create(sparql,model);
        ResultSet result = qexec.execSelect();
        while(result.hasNext()){
            QuerySolution sol = result.nextSolution();
            quantity+=sol.get("?number").asLiteral().getInt();
        }
//        System.out.println("read quantity "+quantity);
        return quantity;
    }
}
