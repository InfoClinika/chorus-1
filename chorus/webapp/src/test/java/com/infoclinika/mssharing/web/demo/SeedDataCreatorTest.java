package com.infoclinika.mssharing.web.demo;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableSet;
import com.infoclinika.mssharing.platform.model.common.items.DictionaryItem;
import com.infoclinika.mssharing.platform.model.helper.ExperimentCreationHelperTemplate;
import com.infoclinika.mssharing.web.helper.AbstractDataBasedTest;
import org.hamcrest.CustomTypeSafeMatcher;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.testng.annotations.Test;

import java.util.Collection;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * @author Pavel Kaplin
 */
public class SeedDataCreatorTest extends AbstractDataBasedTest {

    @Test
    public void testExperimentTypes() {
        final Collection<DictionaryItem> types = Collections2.transform(helper.experimentTypes(), new Function<ExperimentCreationHelperTemplate.ExperimentTypeItem, DictionaryItem>() {
            @Override
            public DictionaryItem apply(ExperimentCreationHelperTemplate.ExperimentTypeItem experimentTypeItem) {
                return new DictionaryItem(experimentTypeItem.id, experimentTypeItem.name);
            }
        });

        assertThat(types, isDictionaryOf("Bottom Up Proteomics", "Metabolomics", "DMPK", "Other", "Top Down Proteomics"));
    }

     @Test
    public void testSpecies() {
        ImmutableSet<DictionaryItem> species = helper.species();
        assertThat(species, isDictionaryOf("Unspecified", "Arabidopsis thaliana", "Escherichia coli", "Pneumocystis carinii",
                "Bos taurus", "Hepatitis C virus", "Rattus norvegicus",
                "Caenorhabditis elegans", "Homo sapiens", "Saccharomyces cerevisiae",
                "Chlamydomonas reinhardtii", "Mus musculus", "Schizosaccharomyces pombe",
                "Danio rerio (zebrafish)", "Mycoplasma pneumoniae", "Takifugu rubripes",
                "Dictyostelium discoideum", "Oryza sativa", "Xenopus laevis",
                "Drosophila melanogaster", "Plasmodium falciparum", "Zea mays",
                "Bemisia tabaci (Gennadius)", "Mucata mulata", "Canis familiaris", "Gallus gallus", "Sus scrofa",
                "Anopheles gambiae", "Caenorhabditis briggsae", "Caenorhabditis remanei", "Haliotis rufescens", "Pseudomonas aeruginosa",
                "Staphylococcus aureus", "Gasterosteus aculeatus (fish)",
                "Trichomonas vaginalis"));
    }

    private Matcher<DictionaryItem> withName(final String name) {
        return new CustomTypeSafeMatcher<DictionaryItem>("Name is " + name) {
            @Override
            protected boolean matchesSafely(DictionaryItem item) {
                return item.name.equals(name);
            }
        };
    }

    private Matcher<Collection<? extends DictionaryItem>> isDictionaryOf(String... values) {
        Matcher[] itemMatchers = new Matcher[values.length];
        int i = 0;
        for (String value : values) {
            itemMatchers[i++] = Matchers.hasItem(withName(value));
        }
        return allOf(hasSize(values.length), allOf(itemMatchers));
    }
}
