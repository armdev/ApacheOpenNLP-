/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.nlp.beans;

import java.io.FileInputStream;
import java.io.InputStream;

import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;

/**
 *
 * @author Admin
 */
public class SentenceDetectionME {

    public static void main(String args[]) throws Exception {

        String sentence = "Hi. How are you? Welcome to Tutorialspoint. "
                + "We provide free tutorials on various technologies";

        //Loading sentence detector model 
        InputStream inputStream = new FileInputStream("D:\\jee8\\ApacheOpenNLP-\\jsf-nlp\\models\\en-sent.bin");
        SentenceModel model = new SentenceModel(inputStream);

        //Instantiating the SentenceDetectorME class 
        SentenceDetectorME detector = new SentenceDetectorME(model);

        //Detecting the sentence
        String sentences[] = detector.sentDetect(sentence);

        //Printing the sentences 
        for (String sent : sentences) {
            System.out.println(sent);
        }
    }
}
