package com.infoclinika.mssharing.integration.test.testdata;

import com.infoclinika.mssharing.integration.test.data.UserData;

/**
 * @author Alexander Orlov
 */
public enum DefaultUser {

    PAVEL_KAPLIN_AT_GMAIL {
        public UserData getData() {
            return new UserData.Builder().email("pavel.kaplin@gmail.com").password("pwd").firstName("Pavel").lastName("Kaplin").build();
        }
    },
    ADMIN {
        public UserData getData() {
            return new UserData.Builder().email("chorusproject.no.reply@gmail.com").password("pwd").firstName("Mark").lastName("Adminovich").build();
        }
    },
    GENE_SIMMONS_AT_GMAIL {
        public UserData getData() {
            return new UserData.Builder().email("chorus.tester@gmail.com").password("pwd").firstName("Gene").lastName("Simmons").build();
        }
    };

    public abstract UserData getData();
}
