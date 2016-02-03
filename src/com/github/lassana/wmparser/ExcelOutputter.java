package com.github.lassana.wmparser;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.WorkbookUtil;

import java.io.*;
import java.util.*;

import static org.apache.commons.lang.StringUtils.trimToEmpty;

/**
 * @author Nikolai Doronin {@literal <lassana.nd@gmail.com>}
 * @since 2/3/16.
 */
public class ExcelOutputter {

    private static final String SHEET_NAME = "Universities Info";
    private static final List<String> UNIVERSITY_INFO_HEADER = Arrays.asList(
            "World Rank",
            "University",
            "University Site",
            "Presence Rank",
            "Impact Rank",
            "Openess Rank",
            "Excellence Rank",
            "Country",
            "Region"
    );

    Workbook wb;
    Sheet sheet;
    Row row;
    short currentRow;
    short currentCell;

    public ExcelOutputter(String fileName, String sheetName) throws IOException {
        this.wb = new HSSFWorkbook(new FileInputStream("xlsFiles/" + fileName));
        this.sheet = wb.getSheet(sheetName);
        this.currentRow = 0;
        this.currentCell = 0;
    }

    public ExcelOutputter() {
        this.wb = new HSSFWorkbook();
        this.sheet = wb.createSheet(
                WorkbookUtil.createSafeSheetName(SHEET_NAME)
        );
        this.currentRow = 0;
        this.currentCell = 0;
    }

    public String getUniversitySite(int rn, int coln) {
        row = sheet.getRow(rn);
        return row.getCell(coln).getStringCellValue();
    }

    public int getRowCount() {
        return sheet.getLastRowNum() + 1;
    }

    public void appendInfo(List<String> line, int rn) {
        row = this.sheet.getRow(rn);
        currentCell = row.getLastCellNum();
        ++currentCell;
        for (String s : line) {
            row.createCell(currentCell).setCellValue(s);
            currentCell++;
        }
    }

    public void addLine(List<String> line) {
        this.row = sheet.createRow(currentRow);
        for (String s : line) {
            setCellValue(s);
            ++currentCell;
        }
        ++currentRow;
        currentCell = 0;
    }

    private void setCellValue(String value) {
        // set num type for cell if num
        if (value.matches("\\d+"))
            row.createCell(currentCell).setCellValue(Double.parseDouble(value));
        else
            row.createCell(currentCell).setCellValue(value);
    }

    public void saveXLS(String filename) throws IOException {
        File file = new File("xlsFiles/" + toSafeWinFileName(filename));
        if ( !file.exists() )
        {
            if (!file.getParentFile().mkdirs() || !file.createNewFile())
            {
                throw new RuntimeException("Cannot create file for output");
            }
        }
        FileOutputStream fileOut = new FileOutputStream(file);
        wb.write(fileOut);
        fileOut.close();
    }

    private String toSafeWinFileName(String proposal) {
        // without escaping "[<>:"/\|?*]"
        String reserved = "[<>:\"/\\\\|\\?\\*]";
        return proposal.replaceAll(reserved, "#");
    }

    //hardcoded order
    public static void universitiesToXLS(String filename, Collection<UniversityInfo> universityInfos)
            throws IOException
    {
        ExcelOutputter outputter = new ExcelOutputter();
        // add header
        outputter.addLine(UNIVERSITY_INFO_HEADER);
        for (UniversityInfo info : universityInfos) {
            outputter.addLine(info.toList());

        }

        outputter.saveXLS(filename);
    }

    //hardcoded order
    public static void universitiesToXLS(String filename, Collection<UniversityInfo> universityInfos, Set<String> setOfTopics)
            throws IOException
    {
        ExcelOutputter outputter = new ExcelOutputter();
        // add header
        List<String> listOfUniqueTopics = new ArrayList<>(setOfTopics.size());
        listOfUniqueTopics.addAll(setOfTopics);

        List<String> header = new ArrayList<>(UNIVERSITY_INFO_HEADER.size() + setOfTopics.size());
        header.addAll(UNIVERSITY_INFO_HEADER);
        header.addAll(setOfTopics);
        outputter.addLine(header);
        for (UniversityInfo info : universityInfos) {
            List<String> line = new ArrayList<>(header.size());
            line.addAll(info.toList());
            Map<String, String> topicsMap = info.getTopicTrust();
            for (String topic : listOfUniqueTopics)
                line.add( trimToEmpty(topicsMap.get(topic)) );
            outputter.addLine(line);
        }

        outputter.saveXLS(filename);
    }

}