package com.infoclinika.mssharing.web.controller.v2.dto;

import com.infoclinika.mssharing.model.read.dto.details.ProcessingRunItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProcessingRunDetails {

   private long id;
   private String name;
   private String processedDate;
   private List<ProcessingFileDTO> processedFiles;


}
