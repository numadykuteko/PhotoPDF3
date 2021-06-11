package com.pdfconverter.jpg2pdf.pdf.converter.data.model;

import java.util.List;

public class SplitPDFOptions {
    private List<SplitFileData> outputList;
    private String inputFilePath;
    private DocumentData inputFileData;

    public SplitPDFOptions(List<SplitFileData> outputList, String inputFilePath, DocumentData inputFileData) {
        this.outputList = outputList;
        this.inputFilePath = inputFilePath;
        this.inputFileData = inputFileData;
    }

    public List<SplitFileData> getOutputList() {
        return outputList;
    }

    public void setOutputList(List<SplitFileData> outputList) {
        this.outputList = outputList;
    }

    public String getInputFilePath() {
        return inputFilePath;
    }

    public void setInputFilePath(String inputFilePath) {
        this.inputFilePath = inputFilePath;
    }

    public DocumentData getInputFileData() {
        return inputFileData;
    }

    public void setInputFileData(DocumentData inputFileData) {
        this.inputFileData = inputFileData;
    }
}
