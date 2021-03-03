package com.arsene.modelservices.services;


import com.google.gson.JsonObject;
import org.eclipse.epsilon.common.module.IModule;
import org.eclipse.epsilon.common.parse.problem.ParseProblem;
import org.eclipse.epsilon.common.util.StringProperties;
import org.eclipse.epsilon.ecl.EclModule;
import org.eclipse.epsilon.emc.emf.EmfModel;
import org.eclipse.epsilon.emc.plainxml.PlainXmlModel;
import org.eclipse.epsilon.eol.EolModule;
import org.eclipse.epsilon.eol.models.IRelativePathResolver;

import org.eclipse.epsilon.etl.EtlModule;
import org.eclipse.epsilon.etl.launch.EtlRunConfiguration;
import org.eclipse.epsilon.evl.EvlModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;


public class EpsilonService {
    public static Logger log = LoggerFactory.getLogger(EpsilonService.class);

    private static final Path ROOT = Paths.get("src/main/");
    private static final Path MODEL_FOLDER = ROOT.resolve("resources/artifacts/models");
    private static final Path METAMODEL_FOLDER = ROOT.resolve("resources/artifacts/metamodels");
    private static final Path SCRIPT_FOLDER = ROOT.resolve("resources/artifacts/script");

    private static final String treeModel = MODEL_FOLDER.resolve("Tree.xmi").toAbsolutePath().toUri().toString();
    private static final String treeMM = METAMODEL_FOLDER.resolve("Tree.ecore").toAbsolutePath().toUri().toString();


    // Epsilon transformation engine
    /*--------------------------------------------------------------------------------------------------------------*/
    public void etlEngine() throws Exception {

        // Setting artifact paths ( name, model path, metamodel path)
        Path etlScript = SCRIPT_FOLDER.resolve("Demo.etl");
        String targetModel = MODEL_FOLDER.resolve("hdsj.xmi").toAbsolutePath().toUri().toString();

        StringProperties sourceProperties = createEmfModel("Source", treeModel, treeMM, "true", "false");
        StringProperties targetProperties = createEmfModel("Target", targetModel, treeMM, "false", "true");

//        boolean checkParser = isParsed("etl", etlScript);
//        System.out.println("Parser status: " + checkParser);


        // Configuring the transformer launcher
        EtlRunConfiguration transformer = EtlRunConfiguration.Builder()
                .withScript(etlScript)
                .withModel(new EmfModel(), sourceProperties)
                .withModel(new EmfModel(), targetProperties)
                .withParameter("parameterPassedFromJava", "Hello from pre")
                .withProfiling()
                .build();

        // Transforming source model into target model
        transformer.run();
        // Making the target model available after running
        transformer.dispose();

        System.out.println(" ----- |||| ------- Finished transformation ----- |||| -------");
        //log.info("Finished ----- |||| -------");
    }


    // Epsilon object query engine
    /*--------------------------------------------------------------------------------------------------------------*/
    public void eolEngine() throws Exception {
        String scriptFileName = "Demo.eol";
        String language = "eol";
        String modelName = "TreeModel";

        // It seems to query the model, we need it metamodel and the model  itself
        Path evlScript = SCRIPT_FOLDER.resolve(scriptFileName);

        EolModule module = (EolModule) isParsed(language, evlScript);

        StringProperties modelProperties = createEmfModel(modelName, treeModel, treeMM, "true", "false");


        OutputStream outputStream = new ByteArrayOutputStream();
        JsonObject jsonObject = new JsonObject();
        EmfModel emfModel = new EmfModel();


        emfModel.load(modelProperties, (IRelativePathResolver) null);
        module.getContext().setOutputStream(new PrintStream(outputStream));
        module.getContext().getModelRepository().addModel(emfModel);


        module.execute();

        String out = outputStream.toString();
        jsonObject.addProperty("output", outputStream.toString());

        System.out.println(out);
    }


    public boolean eclEngine() throws Exception {

        String model1 = "src/main/resources/artifacts/models/catalogue1.xml";
        String model2 = "src/main/resources/artifacts/models/catalogue2.xml";


        String scriptFileName = "Demo.ecl";
        String language = "ecl";

        // It seems to query the model, we need it metamodel and the model  itself
        Path eclScript = SCRIPT_FOLDER.resolve(scriptFileName);

        EclModule module = (EclModule) isParsed(language, eclScript);

        PlainXmlModel catalogue1 = new PlainXmlModel();
        catalogue1.setName("Catalogue1");
        catalogue1.setFile(new File(model1));
        catalogue1.load();

        PlainXmlModel catalogue2 = new PlainXmlModel();
        catalogue2.setName("Catalogue2");
        catalogue2.setFile(new File(model2));
        catalogue2.load();


        module.getContext().getModelRepository().addModels(catalogue1, catalogue2);
        module.execute();

        System.out.println(module.getContext().getMatchTrace().getReduced() != null);
//        System.out.println(catalogue1.getXml());

        return module.getContext().getMatchTrace().getReduced() != null;

    }


    // Epsilon model validation engine
    /*--------------------------------------------------------------------------------------------------------------*/
    public void evlEngine() throws Exception {

        String scriptFileName = "Demo.evl";
        String language = "evl";
        String modelName = "TreeModel";

        // It seems to query the model, we need it metamodel and the model  itself
        Path evlScript = SCRIPT_FOLDER.resolve(scriptFileName);

        EvlModule module = (EvlModule) isParsed(language, evlScript);

        StringProperties modelProperties = createEmfModel(modelName, treeModel, treeMM, "true", "false");


        OutputStream outputStream = new ByteArrayOutputStream();
        JsonObject jsonObject = new JsonObject();
        EmfModel emfModel = new EmfModel();


        emfModel.load(modelProperties, (IRelativePathResolver) null);
        module.getContext().setOutputStream(new PrintStream(outputStream));
        module.getContext().getModelRepository().addModel(emfModel);


        module.execute();

        String out = outputStream.toString();
        jsonObject.addProperty("output", outputStream.toString());

        System.out.println(out);

    }


    // ---------------------------------------------------------- UTILITIES -------------------------------------------------------------------------------

    public StringProperties createEmfModel(String modelName, String modelPath, String metaModelPath, String readOnLoad, String storeOnDisposal) {
        // Setting model properties ( name, model path, metamodel path)
        StringProperties property = new StringProperties();
        property.setProperty(EmfModel.PROPERTY_NAME, modelName);
        property.setProperty(EmfModel.PROPERTY_FILE_BASED_METAMODEL_URI, metaModelPath);
        property.setProperty(EmfModel.PROPERTY_MODEL_URI, modelPath);

        // Configuring the target model properties (model, metamodel)
        /*  READONLOAD: If the model(file) is there, it will not read it or ignore based on the flag indicated  */
        /*  STOREONDISPOSAL: We can read the model but not need to save it, or we save it based on the flag indicated*/
        property.setProperty(EmfModel.PROPERTY_READONLOAD, readOnLoad);
        property.setProperty(EmfModel.PROPERTY_STOREONDISPOSAL, storeOnDisposal);

        return property;
    }

    public IModule isParsed(String moduleType, Path scriptPath) throws Exception {

        IModule module = null;

        switch (moduleType) {
            case "etl":
                module = new EtlModule();
                break;
            case "eol":
                module = new EolModule();
                break;
            case "evl":
                module = new EvlModule();
                break;
            case "ecl":
                module = new EclModule();
                break;
            default:
                module = null;
        }

        boolean isParsed = module.parse(scriptPath);

//        System.out.println("Passed: " + isParsed);

        //In case there is parser errors, print them
        if (!isParsed == false)
            for (ParseProblem problem : module.getParseProblems())
                System.err.println(problem.toString());

        return module;
    }
}


