package com.github.lassana.wmparser;

import org.apache.log4j.Logger;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

/**
 * @author Nikolai Doronin {@literal <lassana.nd@gmail.com>}
 * @since 2/3/16.
 */
public class WebometricsParser  {

    private static final Logger log = Logger.getLogger(WebometricsParser.class);
    private static String WEBOMETRICS = "http://webometrics.info/en/";
    private Set<UniversityInfo> universityInfoSet = new HashSet<>(24000);

    private static Set<String> oddCountries = new HashSet<>(
            Arrays.asList("natx", "north", "natsouth", "natnorth", "natmidwest", "natwest", "libx", "libnorth", "libsouth", "libmidwest", "libwest")
    );
    private static Set<String> oddTabs = new HashSet<>(
            Arrays.asList("en", "18")
    );

    // maps encoded region to encoded list of countries which belongs to region
    private Map<String, List<String> > region_EncCounties;
    // maps encoded ( part of href ) region name to normal name
    private Map<String, String> encRegion_Region = new HashMap<>(8);

    // omg hack !
    private int pos;
    private int countOfUniversities;
    private long sleepTime = 1000;

    public WebometricsParser() {
        try {
            fillMaps();
        } catch (IOException e) {
            throw new RuntimeException("Can't fill maps", e);
        }
    }

    public Map<String, List<String> > getRegion_EncCounties() {
        return region_EncCounties;
    }

    public Map<String, String> getEncRegion_Region() {
        return encRegion_Region;
    }

    public Collection<UniversityInfo> getAllUniversitiesInfo() {
        for (String region : region_EncCounties.keySet())
            for (String country : region_EncCounties.get(region)) {
                try {
                    parseUniversities(region, country);
                } catch (IOException e) {
                    log.error("Can't parse region, country pair: " + "[" + region + ", " + country + "]", e);
                }
            }

        return universityInfoSet;
    }

    private void fillMaps() throws IOException {
        region_EncCounties = new HashMap<>();
        Document jdoc = getDocument(WEBOMETRICS);
        Elements regions = jdoc.select("li.megamenu-li-first-level");
        for (Element e : regions) {
            Elements regionBlocks = e.getElementsByTag("a");
            String regionRef = regionBlocks.attr("href");
            String region = regionRef.substring(regionRef.lastIndexOf('/') + 1);
            String regionName = regionBlocks.get(0).text();
            if (!oddTabs.contains(region)) {
                List<String> countries = getCountries(e, region);
                countries.removeAll(oddCountries);
                encRegion_Region.put(region, regionName);
                region_EncCounties.put(region, countries);
            }
        }
    }

    private List<String> getCountries(Element regionBlock, String region) {
        Elements sections = regionBlock.select("ul.megamenu-section");
        List<String> list = new ArrayList<>(30);
        for (Element e : sections) {
            for (Element c : e.getElementsByTag("li")) {
                String countryRef = c.getElementsByTag("a").attr("href");
                String country = countryRef.replace(
                        "/en/" + region + "/",
                        ""
                );

                list.add(country);
            }
        }

        return list;
    }

    private int getCountOfPages(Document jdoc) {
        String lastPageHref = jdoc.select("li.pager-last.last").select("a").attr("href");
        String lastPageNum = lastPageHref.substring(lastPageHref.indexOf('=') + 1);
        int countOfPages;
        if (lastPageNum.isEmpty())
            countOfPages = 1;
        else
            countOfPages = Integer.parseInt(lastPageNum) + 1;

        return countOfPages;
    }

    public List<UniversityInfo> parseUniversities(String region, String country) throws IOException {
        log.info("Start parsing for [ " + region + ", " + country +  " ]");
        List<UniversityInfo> list = new ArrayList<>(10000);

        Document jdoc = getDocument(region, country, 0);
        int countOfPages = getCountOfPages(jdoc);
        list.addAll(parseBody(jdoc, region, country));
        log.info("count of pages: " + countOfPages);

        for (int i = 1; i < countOfPages; i++) {
            list.addAll( parseBody(getDocument(region, country, i), region, country) );
        }

        log.info("End parsing for [ " + region + ", " + country +  " ]");
        return list;
    }

    private Document getDocument(String region, String country, int pageNum) throws IOException{
        String path = WEBOMETRICS + region + "/" +
                country + "?page=" + pageNum;
        log.info("path: " + path);
        return getDocument(path);
    }

    private Document downloadAndParse(String path) throws IOException {
        log.info("Let's get " + path);
        long start = System.currentTimeMillis();
        Connection.Response response = Jsoup.connect(path)
                .timeout(5000)
                .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.152 Safari/537.36")
                .execute();
        long end = System.currentTimeMillis();
        long elapsedTime = end - start;
        log.info("Got it; Code = " + response.statusCode() + "; elapsed time: " + elapsedTime);
        return response.parse();
    }

    private Document getDocument(String path) throws IOException {
        boolean needTryAgain;
        Document rvalue = null;
        do {
            try {
                rvalue = downloadAndParse(path);
                needTryAgain = false;
            } catch (Exception e) {
                log.info(String.format("Exception :(\n%s\nLet's get sleep for %d", e.getMessage(), sleepTime));
                try {
                    Thread.sleep(sleepTime);
                    sleepTime += 500;
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                    Thread.currentThread().interrupt();
                }
                needTryAgain = true;
            }
        } while (needTryAgain);
        return  rvalue;
    }

    //take href region and country names args; decoding country and getting region from encRegion_Region map
    private List<UniversityInfo> parseBody(Document jdoc, String region, String country) {
        UniversityInfo info;
        region = encRegion_Region.get(region);
        try {
            country = URLDecoder.decode(country, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.error("Can't decode country" + country, e);
        }
        List<UniversityInfo> list = new ArrayList<>(1000);
        Elements body = jdoc.select("div.region.region.region-content").select("tbody");
        Elements rows = body.select("tr");
        log.trace("Table with universities, Body: " + body.html());
        //return empty list if no universities in curr country
        if (rows.size() == 1 && "There is no element".equals(rows.get(0).text()) )
            return list;
        for (Element e : rows) {
            Elements elems = e.select("td");
            info = new UniversityInfo();

            pos = 1;
            info.setWorldRank( getCellValue(elems) ); ++pos;
            info.setUniversity( getCellValue(elems) );
            info.setUniversitySite( getCellHref(elems) ); ++pos;

            info.setPresenceRank( getCellValue(elems) ); ++pos;
            info.setImpactRank( getCellValue(elems) ); ++pos;
            info.setOpenessRank( getCellValue( elems) ); ++pos;
            info.setExcellenceRank( getCellValue( elems) ); ++pos;

            info.setCountry(country);
            info.setRegion(region);

            list.add(info);
            universityInfoSet.add(info);
        }

        countOfUniversities += list.size();

        return list;
    }

    /**
     * @param elems tr, th collection
     * @return value of cell
     */
    private String getCellValue(Elements elems) {
        String res = null;
        try {
            Element content = elems.get(pos).getAllElements().get(0);
            if (content == null)
                throw new RuntimeException("No such elem for pos: " + pos);
            res = content.text();
            if (res == null || res.trim().isEmpty()) {
                ++pos;

                log.trace("Extracted value is null; shifting pos: " + pos);
                log.trace("html of curr elems:" + elems.html());

                res = getCellValue(elems);
            }
        } catch (Exception e) {
            log.error("elems html:\n" + elems.html(), e);
        }
        return res;
    }

    /**
     * @param elems tr or th elem to parse
     * @return href of cell
     */
    private String getCellHref(Elements elems) {
        Element content = elems.get(pos).getAllElements().get(0);
        if (content == null)
            throw new RuntimeException("No such elem for pos: " + pos);
        String res = content.getElementsByTag("a").attr("href");
        //String res = content.getElementsByTag("a").attr("href").replaceAll("http(s)?://(www\\.)?", "");
        if (res == null || res.trim().isEmpty()) {
            ++pos;
            log.trace("Extracted value is null; shifting pos: " + pos);
            log.trace("html of curr elems:" + elems.html());
            res = getCellHref(elems);
        }
        if (res.endsWith("/"))
            res = res.substring(0, res.length() - 1);

        return res;
    }

}