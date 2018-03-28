package com.infoclinika.mssharing.web.rest;

import com.infoclinika.mssharing.dto.request.ConfirmMultipartUploadDTO;
import com.infoclinika.mssharing.dto.request.UploadFilesDTORequest;
import com.infoclinika.mssharing.dto.request.UserNamePassDTO;
import com.infoclinika.mssharing.dto.response.*;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Set;

/**
 * @author timofey.kasyanov
 *         date: 19.03.2014
 */
@Path("/uploaderAPI")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.WILDCARD)
public interface UploaderRestService {

    @Path("/authenticate")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    AuthenticateDTO authenticate(UserNamePassDTO credentials);

    @Path("/authenticate")
    @GET
    AuthenticateDTO authenticate(@QueryParam("token") String token);

    @Path("/getTechnologyTypes")
    @GET
    Set<DictionaryDTO> getTechnologyTypes(@QueryParam("token") String token);

    @Path("/getVendors")
    @GET
    Set<DictionaryDTO> getVendors(@QueryParam("token") String token);

    @Path("/getLabs")
    @GET
    Set<DictionaryDTO> getLabs(@QueryParam("token") String token);

    @Path("/getInstrumentModels")
    @GET
    Set<DictionaryDTO> getInstrumentModels(
            @QueryParam("token") String token,
            @QueryParam("technologyType") long technologyType,
            @QueryParam("vendor") long vendor);

    @Path("/getInstrumentsByModel")
    @GET
    List<InstrumentDTO> getInstruments(@QueryParam("token") String token, @QueryParam("instrumentModel") long instrumentModel);

    @Path("/getInstrument/{id}")
    @GET
    InstrumentDTO getInstrument(@PathParam("id") long instrumentId, @QueryParam("token") String token);

    @Path("/createDefaultInstrument")
    @POST
    InstrumentDTO createDefaultInstrument(@QueryParam("lab") long lab, @QueryParam("instrumentModel") long instrumentModel, String token);

    @Path("/getInstruments")
    @GET
    List<InstrumentDTO> getInstruments(@QueryParam("token") String token);

    @Path("/getInstrumentFiles/{id}")
    @GET
    Set<FileDTO> getInstrumentFiles(@PathParam("id") Long instrumentId, @QueryParam("token") String token);

    @Path("/isReadyToUpload")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    FilesReadyToUploadResponse isReadyToUpload(FilesReadyToUploadRequest request, @QueryParam("token") String token);

    @Path("/composeFiles")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    ComposeFilesResponse composeFiles(ComposeFilesRequest request, @QueryParam("token") String token);

    @Path("/getSpecies")
    @GET
    Set<DictionaryDTO> getSpecies(@QueryParam("token") String token);

    @Path("/getDefaultSpecie")
    @GET
    DictionaryDTO getDefaultSpecie(@QueryParam("token") String token);

    @Path("/getUnfinishedUploads")
    @GET
    List<FileDTO> getUnfinishedUploads(@QueryParam("token") String token);

    @Path("/deleteUpload")
    @DELETE
    DeleteUploadDTO deleteUpload(@QueryParam("fileId") Long fileId, @QueryParam("token") String token);

    @Path("/uploadRequest")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    UploadFilesDTOResponse uploadRequest(UploadFilesDTORequest request, @QueryParam("token") String token);

    @Path("/sseUploadRequest")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    SSEUploadFilesDTOResponse sseUploadRequest(UploadFilesDTORequest request, @QueryParam("token") String token);

    @Path("/completeUploadFileRequest")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    CompleteUploadDTO completeUpload(ConfirmMultipartUploadDTO request, @QueryParam("token") String token);

}
