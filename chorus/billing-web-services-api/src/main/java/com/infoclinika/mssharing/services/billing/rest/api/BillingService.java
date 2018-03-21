package com.infoclinika.mssharing.services.billing.rest.api;

import com.infoclinika.mssharing.services.billing.rest.api.model.*;

import javax.annotation.Nullable;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * @author andrii.loboda
 */
@Path("/")
@Produces({MediaType.APPLICATION_JSON})
public interface BillingService {
    @GET
    @Path("healthCheck")
    String healthCheck();

    @POST
    @Path("logProteinIDSearchUsage")
    void logProteinIDSearchUsage(@FormParam("user") long user,
                                 @FormParam("experiment") long experiment);

    @POST
    @Path("depositStoreCredit")
    @Consumes(MediaType.APPLICATION_JSON)
    void depositStoreCredit(DepositStoreCreditRequest request);


    @POST
    @Path("logDownloadUsage")
    void logDownloadUsage(@FormParam("actor") long actor,
                          @FormParam("file") long file,
                          @FormParam("lab") long lab);

    @POST
    @Path("logPublicDownload")
    void logPublicDownload(@FormParam("actor") @Nullable Long actor,
                           @FormParam("file") long file);

    @GET
    @Path("readInvoiceShortItem")
    Invoice readInvoiceShortItem(@QueryParam("actor") long actor,
                                 @QueryParam("lab") long lab,
                                 @QueryParam("from") long from,
                                 @QueryParam("to") long to);

    @GET
    @Path("readLabsForUser")
    Collection<InvoiceLabLine> readLabsForUser(@QueryParam("actor") long actor);

    @GET
    @Path("readMonthsReferences")
    HistoryForMonthReference readMonthsReferences(@QueryParam("actor") long actor, @QueryParam("lab") long lab, @QueryParam("month") long month);

    @GET
    @Path("readLabDetails")
    LabInvoiceDetails readLabDetails(@QueryParam("actor") long actor,
                                     @QueryParam("lab") long lab);

    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    @Path("readPagedAllLabs")
    PagedItemInfo.PagedItem<InvoiceLabLine> readPagedAllLabs(ReadPagedAllLabsRequest request);

    @GET
    @Path("readFeatureInfo")
    BillingFeatureItem readFeatureInfo(@QueryParam("feature") long feature);

    @GET
    @Path("readFeatures")
    List<BillingFeatureItem> readFeatures();

    @GET
    @Path("calculateRoundedPriceByUnscaled")
    long calculateRoundedPriceByUnscaled(@QueryParam("actualBalance") long actualBalance,
                                         @QueryParam("unscaledValue") long unscaledValue);

    @GET
    @Path("calculateTotalToPayForLabForDay")
    long calculateTotalToPayForLabForDay(@QueryParam("lab") long lab,
                                         @QueryParam("day") long day);

    /*Returns Nullable instead of Optional. Should be converted to Optional. Such functionality is not supported in web services*/
    @GET
    @Path("calculateStoreBalanceForDay")
    @Nullable
    Long calculateStoreBalanceForDay(@QueryParam("lab") long lab,
                                     @QueryParam("day") long day);

    @GET
    @Path("calculateTotalToPayForLab")
    long calculateTotalToPayForLab(@QueryParam("lab") long lab,
                                   @QueryParam("from") long from,
                                   @QueryParam("to") long to);

    /*Returns Nullable instead of Optional. Should be converted to Optional. Such functionality is not supported in web services*/
    @GET
    @Path("calculateStoreBalance")
    @Nullable
    Long calculateStoreBalance(@QueryParam("lab") long lab,
                               @QueryParam("current") long current,
                               @QueryParam("nextDay") long nextDay);

    @GET
    @Path("getDailyUsageLine")
    @Nullable
    DailyUsageLine getDailyUsageLine(@QueryParam("lab") long lab, @QueryParam("day") long day);

    @POST
    @Path("logLabBecomeEnterprise")
    void logLabBecomeEnterprise(@FormParam("actor") long actor,
                               @FormParam("lab") long lab,
                               @FormParam("time") long time);

    @POST
    @Path("logLabBecomeFree")
    void logLabBecomeFree(@FormParam("actor") long actor,
                          @FormParam("lab") long lab,
                          @FormParam("time") long time);

    @POST
    @Path("logProcessingUsage")
    void logProcessingUsage(@FormParam("actor") long actor,
                            @FormParam("lab")long lab,
                            @FormParam("time") long time);

    @POST
    @Path("storeCreditForLab")
    void storeCreditForLab(@FormParam("admin") long admin,
                           @FormParam("lab")long lab,
                           @FormParam("amount") long amount);

    @GET
    @Path("getPendingChargesForLab")
    @Nullable
    List<PendingCharge> getPendingChargesForLab(@QueryParam("actor") long actor,@QueryParam("lab") long lab, @QueryParam("timestamp") long timestamp);

    @PUT
    @Path("runMigration")
    void runMigration(@FormParam("admin") long admin);

    @GET
    @Path("readStorageUsage")
    StorageUsage readStorageUsage(@QueryParam("actor") long actor, @QueryParam("lab") long lab);

   /* @GET
    @Path("checkIfFeatureIsActiveForLab")
    boolean checkIfFeatureIsActiveForLab(@QueryParam("lab") long lab, @QueryParam("feature") String feature);
*/

    class DepositStoreCreditRequest {
        public Map<String, String> paramsMap;

        public DepositStoreCreditRequest(Map<String, String> paramsMap) {
            this.paramsMap = paramsMap;
        }

        public DepositStoreCreditRequest() {
        }
    }

    class ReadPagedAllLabsRequest {
        public long actor;
        public PagedItemInfo wsPpagedItemInfo;

        public ReadPagedAllLabsRequest(long actor, PagedItemInfo wsPpagedItemInfo) {
            this.actor = actor;
            this.wsPpagedItemInfo = wsPpagedItemInfo;
        }

        public ReadPagedAllLabsRequest() {
        }
    }
}
