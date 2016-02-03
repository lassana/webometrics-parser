package com.github.lassana.wmparser;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.*;

public class Main {

    private static Logger log = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        final long startTime = System.currentTimeMillis();
        WebometricsParser parser = new WebometricsParser();
        Collection<UniversityInfo> collection = parser.getAllUniversitiesInfo();
        try {
            ExcelOutputter.universitiesToXLS(String.format("%s-%d.xls", "GlobalUniversityInfo", System.currentTimeMillis()), collection);
        } catch (IOException e) {
            log.error(e);
        }

        log.info("Amount of universities in Set: " + collection.size());
        log.info("Time elapsed: " +
                (double) (System.currentTimeMillis() - startTime) / 1000 + " seconds"
        );
    }
}
