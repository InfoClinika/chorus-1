package com.infoclinika.mssharing.web.demo;

import com.infoclinika.mssharing.model.PredefinedDataCreator;
import com.infoclinika.mssharing.model.internal.RuleValidator;
import com.infoclinika.mssharing.platform.model.AccessDenied;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.infoclinika.analysis.storage.cloud.CloudStorageItemReference.CLOUD_REFERENCE_URL_SEPARATOR;

/**
 * @author andrii.loboda
 */
@Component
@Singleton
@DependsOn("demoDataCreator")
public class RunDemoDataCreator {
    private static final Logger LOG = LoggerFactory.getLogger(RunDemoDataCreator.class);
    private static final String ESCHERICHIA_COLI = "Escherichia coli";

    @Inject
    private PredefinedDataCreator initiator;
    @Inject
    private RuleValidator ruleValidator;

    @Value("${protein.dbs.target.folder}")
    private String proteinDatabasesPrefix;

    public void createProteinDatabases(long admin) {
        initiator.proteinDatabase(admin, "Homo Sapiens", proteinDatabasesPrefix + CLOUD_REFERENCE_URL_SEPARATOR + "HomoSapiens.fasta", "Homo sapiens");
        initiator.proteinDatabase(admin, "Arabidopsis thaliana", proteinDatabasesPrefix + CLOUD_REFERENCE_URL_SEPARATOR + "ArabidopsisThaliana.fasta", "Arabidopsis thaliana");
        initiator.proteinDatabase(admin, "Bos taurus", proteinDatabasesPrefix + CLOUD_REFERENCE_URL_SEPARATOR + "BosTaurus.fasta", "Bos taurus");
        initiator.proteinDatabase(admin, "Caenorhabditis elegans", proteinDatabasesPrefix + CLOUD_REFERENCE_URL_SEPARATOR + "CaenorhabditisElegans.fasta", "Caenorhabditis elegans");
        initiator.proteinDatabase(admin, "Danio rerio", proteinDatabasesPrefix + CLOUD_REFERENCE_URL_SEPARATOR + "DanioRerio.fasta", "Danio rerio (zebrafish)");
        initiator.proteinDatabase(admin, "Drosophila melanogaster", proteinDatabasesPrefix + CLOUD_REFERENCE_URL_SEPARATOR + "DrosophilaMelanogaster.fasta", "Drosophila melanogaster");
        initiator.proteinDatabase(admin, "Mus musculus", proteinDatabasesPrefix + CLOUD_REFERENCE_URL_SEPARATOR + "MusMusculus.fasta", "Mus musculus");
        initiator.proteinDatabase(admin, "Rattus norvegicus", proteinDatabasesPrefix + CLOUD_REFERENCE_URL_SEPARATOR + "RattusNorvegicus.fasta", "Rattus norvegicus");
        initiator.proteinDatabase(admin, "Saccharomyces cerevisiae", proteinDatabasesPrefix + CLOUD_REFERENCE_URL_SEPARATOR + "SaccharomycesCerevisiae.fasta", "Saccharomyces cerevisiae");
        initiator.proteinDatabase(admin, ESCHERICHIA_COLI, proteinDatabasesPrefix + CLOUD_REFERENCE_URL_SEPARATOR + "EscherichiaColi.fasta", ESCHERICHIA_COLI);
        initiator.proteinDatabase(admin, "Mouse IPI", proteinDatabasesPrefix + CLOUD_REFERENCE_URL_SEPARATOR + "MouseIPI.fasta", "Mus musculus");
    }

    public void createCdfDatabases(long admin) {
        if (!ruleValidator.hasAdminRights(admin)) {
            throw new AccessDenied("User should be admin to create CDF databases");
        }
        LOG.info("Creating CDF databases, actor: " + admin);
        final String specie = "Homo sapiens";
        //see http://brainarray.mbni.med.umich.edu/Brainarray/Database/CustomCDF/20.0.0/entrezg.asp
        initiator.cdfDatabase(admin, "HGU133A2", "http://mbni.org/customcdf/20.0.0/entrezg.download/HGU133A2_Hs_ENTREZG_20.0.0.zip", specie);
        initiator.cdfDatabase(admin, "hugene10st", "http://mbni.org/customcdf/20.0.0/entrezg.download/hugene10st_Hs_ENTREZG_20.0.0.zip", specie);
        initiator.cdfDatabase(admin, "hugene11st", "http://mbni.org/customcdf/20.0.0/entrezg.download/hugene11st_Hs_ENTREZG_20.0.0.zip", specie);
        initiator.cdfDatabase(admin, "hugene20st", "http://mbni.org/customcdf/20.0.0/entrezg.download/hugene20st_Hs_ENTREZG_20.0.0.zip", specie);
        initiator.cdfDatabase(admin, "hugene21st", "http://mbni.org/customcdf/20.0.0/entrezg.download/hugene21st_Hs_ENTREZG_20.0.0.zip", specie);
        LOG.info("Completed creating CDF databases");
    }

}
