package com.nlp.beans;

import com.nlp.entities.People;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.bean.ViewScoped;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;

@ManagedBean
@SessionScoped
public class SentenceModelBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private SentenceModel sentenceModel;
    private SentenceDetectorME sentenceDetector;
    private TokenizerME tokenizer;
    private TokenizerModel tokenizerModel;
    private NameFinderME nameFinder;
    private TokenNameFinderModel nameFinderModel;
    private POSModel partOfSpeechModel;
    //my
    private NameFinderME nameFinderOrg;
    private TokenNameFinderModel orgNameFinderModel;

    private POSTaggerME partOfSpeechTagger;
    //private String filePath = "E:/projects/ApacheNLP_others/berlin-buzzwords-2013-master/opennlp-example/models";
    private String filePath = "E:/projects/a_group_projects/ApacheNLP_others/jsf-nlp/models";
    private String document;
    private List<People> peopleList = new ArrayList<People>();

    public SentenceModelBean() {
    }

    @PostConstruct
    public void init() {
        try {
            initModels();
            tokenizer = new TokenizerME(tokenizerModel);
            //my
            nameFinderOrg = new NameFinderME(orgNameFinderModel);

            nameFinder = new NameFinderME(nameFinderModel);

            partOfSpeechTagger = new POSTaggerME(partOfSpeechModel);

            sentenceDetector
                    = new SentenceDetectorME(
                            new SentenceModel(new FileInputStream(filePath + "/en-sent.bin")));

        } catch (FileNotFoundException ex) {
            Logger.getLogger(SentenceModelBean.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SentenceModelBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   //http://www.programcreek.com/2012/05/opennlp-tutorial/
    //http://opennlp.apache.org/documentation/1.5.2-incubating/manual/opennlp.html#tools.namefind.eval.tool
    
    private void initModels() throws IOException {
        try {
            InputStream sentenceModelStream = new FileInputStream(filePath + "/en-sent.bin");
            InputStream tokenizereModelStream = new FileInputStream(filePath + "/en-token.bin");
            InputStream nameFinderModelStream = new FileInputStream(filePath + "/en-ner-person.bin");
            InputStream partOfSpeechModelStream = new FileInputStream(filePath + "/en-pos-maxent.bin");
            InputStream orgNameFinderStream = new FileInputStream(filePath + "/en-ner-organization.bin");

            sentenceModel = new SentenceModel(sentenceModelStream);
            tokenizerModel = new TokenizerModel(tokenizereModelStream);
            orgNameFinderModel = new TokenNameFinderModel(orgNameFinderStream);
            nameFinderModel = new TokenNameFinderModel(nameFinderModelStream);
            partOfSpeechModel = new POSModel(partOfSpeechModelStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void findPeopleNames() {
        People people = new People();
        String doc = document;
        System.out.println("Doc " + doc);
        if (doc != null) {
            for (String sentence : segmentSentences(doc)) {
              //  System.out.println("1. get sentences: " + sentence);
                String[] tokens = tokenizeSentence(sentence);
               // System.out.println("2. get tokens: " + tokens);
                Span[] spans = findNames(tokens);
                for (Span span : spans) {
                    System.out.print("person: ");
                 
                    for (int i = span.getStart(); i < span.getEnd(); i++) {
                           people = new People();
                        //System.out.println("span.getStart() : " + span.getStart());
                       // System.out.println("span " + span);
                          people.setName(tokens[i]);
                        System.out.print("token : " + tokens[i]);
                        
                        if (i < span.getEnd()) {
                            System.out.print(" ");
                        }
                        
                      
                        peopleList.add(people);
                    }
                    System.out.println();
                }
            }
        }
    }

    public String clear() {
        peopleList.clear();
        document = null;
        return "intro";
    }
    public static void main(String args[]){
        SentenceModelBean model = new SentenceModelBean();
        try {
            model.trainNames();
        } catch (IOException ex) {
            Logger.getLogger(SentenceModelBean.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void trainNames() throws IOException
    {
        Charset charset = Charset.forName("UTF-8");
        ObjectStream<String> lineStream =new PlainTextByLineStream(new FileInputStream(filePath + "/en-ner-person.bin"), charset);
        
        ObjectStream<NameSample> sampleStream = new NameSampleDataStream(lineStream);        
        TokenNameFinderModel model = NameFinderME.train("en", "person", sampleStream, Collections.<String, Object>emptyMap(),100,5);
        NameFinderME nfm = new NameFinderME(model);
        String sentence = "";


        BufferedReader br = new BufferedReader(new FileReader(filePath+ "/names"));
        
        try
         {
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null)
            {
                sb.append(line);
                sb.append('\n');
                line = br.readLine();
            }
            sentence = sb.toString();
         } 
        finally
        {
            br.close();
        }

        InputStream is1 = new FileInputStream(filePath + "/en-token.bin");
        TokenizerModel model1 = new TokenizerModel(is1);

        Tokenizer tokenizer = new TokenizerME(model1);

        String tokens[] = tokenizer.tokenize(sentence);

        for (String a : tokens)
            System.out.println(a);

        Span nameSpans[] = nfm.find(tokens);
        for(Span s: nameSpans)
        {
            System.out.print(s.toString());
            System.out.print(" ");
            for(int index = s.getStart();index < s.getEnd();index++)
            {
                System.out.print(tokens[index] + " ");
            }
            System.out.println(" ");
        }
    }


    public List<People> getPeopleList() {
        return peopleList;
    }

    public String[] tokenizeSentence(String sentence) {
        return tokenizer.tokenize(sentence);
    }

    public Span[] findNames(String[] tokens) {
        return nameFinder.find(tokens);
    }

    public String[] segmentSentences(String document) {
        return sentenceDetector.sentDetect(document);
    }

    public String getDocument() {
        return document;
    }

    public void setDocument(String document) {
        this.document = document;
    }

    public Span[] findOrgNames(String[] tokens) {
        return nameFinderOrg.find(tokens);
    }

    public String[] tagPartOfSpeech(String[] tokens) {
        return partOfSpeechTagger.tag(tokens);
    }

    public double[] getPartOfSpeechProbabilities() {
        return partOfSpeechTagger.probs();
    }
}
