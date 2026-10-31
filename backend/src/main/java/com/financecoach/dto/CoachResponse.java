package com.financecoach.dto;
public class CoachResponse { private String response; private String source="deterministic"; public CoachResponse(){} public CoachResponse(String response){this.response=response;} public String getResponse(){return response;} public void setResponse(String v){response=v;} public String getSource(){return source;} public void setSource(String v){source=v;} }
