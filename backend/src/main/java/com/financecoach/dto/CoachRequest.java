package com.financecoach.dto;
import jakarta.validation.constraints.Size;
public class CoachRequest { @Size(max=2000) private String message; private String question; public String getMessage(){return message;} public void setMessage(String v){message=v;} public String getQuestion(){return question;} public void setQuestion(String v){question=v;} public String text(){return message!=null&&!message.isBlank()?message:question;} }
