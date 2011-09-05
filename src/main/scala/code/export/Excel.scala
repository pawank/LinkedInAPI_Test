package code.export 

import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel._
import java.io.{ IOException, FileOutputStream }
import org.slf4j.LoggerFactory
import code.model._


object Excel {
    def logger = LoggerFactory.getLogger("Excel")

  def cleanMe(str:String):String = { 
    str.replaceAll("&lt;","<").replaceAll("&gt;",">").replaceAll("&quot;","'").replaceAll("&amp;","&")
  }

  def getUserDetailExcel(name: String, list: List[UserSocialProfile], path: String): Unit = {
    logger.info("Starting preparing excel: " + name)
    var wb: Workbook = new HSSFWorkbook();
    var sheet: Sheet = wb.createSheet(name);
    sheet.setPrintGridlines(false);
    sheet.setDisplayGridlines(false);

    var printSetup: PrintSetup = sheet.getPrintSetup();
    printSetup.setLandscape(true);
    sheet.setFitToPage(true);
    sheet.setHorizontallyCenter(true);

    var tfont: Font = wb.createFont();
    tfont.setBoldweight(Font.BOLDWEIGHT_BOLD);

    var createHelper: CreationHelper = wb.getCreationHelper();
    var cellStyle: CellStyle = wb.createCellStyle();
    cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
    cellStyle.setBorderTop(CellStyle.BORDER_THIN);
    cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
    cellStyle.setBorderRight(CellStyle.BORDER_THIN);

    var cnt = 1
    for (i <- list) {
      if (cnt == 1) {
        var row: Row = sheet.createRow(cnt);
        row.createCell(0).setCellStyle(cellStyle);
        row.createCell(0).setCellValue("SERIAL NO");
        row.createCell(1).setCellStyle(cellStyle);
        row.createCell(1).setCellValue("FIRST NAME");
        row.createCell(2).setCellStyle(cellStyle);
        row.createCell(2).setCellValue("LAST NAME");
        row.createCell(3).setCellStyle(cellStyle);
        row.createCell(3).setCellValue("HEADLINE");
        row.createCell(4).setCellStyle(cellStyle);
        row.createCell(4).setCellValue("INTERESTS");
        row.createCell(5).setCellStyle(cellStyle);
        row.createCell(5).setCellValue("INDUSTRY");
        row.createCell(6).setCellStyle(cellStyle);
        row.createCell(6).setCellValue("LOCATION");
        row.createCell(7).setCellStyle(cellStyle);
        row.createCell(7).setCellValue("NO CONNECTIONS");
        row.createCell(8).setCellStyle(cellStyle);
        row.createCell(8).setCellValue("SOCIAL ID");
        row.createCell(9).setCellStyle(cellStyle);
        row.createCell(9).setCellValue("ADDRESS");
        row.createCell(10).setCellStyle(cellStyle);
        row.createCell(10).setCellValue("PHONE NOS");
        row.createCell(11).setCellStyle(cellStyle);
        row.createCell(11).setCellValue("SUMMARY");
        row.createCell(12).setCellStyle(cellStyle);
        row.createCell(12).setCellValue("SPL");
        row.createCell(13).setCellStyle(cellStyle);
        row.createCell(13).setCellValue("COMPANY WITH POSITIONS");
      }

      var row: Row = sheet.createRow(cnt + 1);

      var c: Cell = row.createCell(0);
      c.setCellStyle(cellStyle);
      c.setCellValue(cnt);

      c = row.createCell(1);
      c.setCellStyle(cellStyle);
      c.setCellValue(i.first_name.is);

      c = row.createCell(2);
      c.setCellStyle(cellStyle);
      c.setCellValue(i.last_name.is);
      
      c = row.createCell(3);
      c.setCellStyle(cellStyle);
      c.setCellValue(createHelper.createRichTextString(cleanMe(i.headline.is)));

      c = row.createCell(4);
      c.setCellStyle(cellStyle);
      c.setCellValue(createHelper.createRichTextString(cleanMe(i.interests.is)));

      c = row.createCell(5);
      c.setCellStyle(cellStyle);
      c.setCellValue(createHelper.createRichTextString(cleanMe(i.industry.is)));

      c = row.createCell(6);
      c.setCellStyle(cellStyle);
      c.setCellValue(createHelper.createRichTextString(i.location.is));

      c = row.createCell(7);
      c.setCellStyle(cellStyle);
      c.setCellValue(createHelper.createRichTextString(i.no_connections.is.toString()));

      c = row.createCell(8);
      c.setCellStyle(cellStyle);
      c.setCellValue(createHelper.createRichTextString(i.social_user_id.is));

      c = row.createCell(9);
      c.setCellStyle(cellStyle);
      c.setCellValue(createHelper.createRichTextString(cleanMe(i.main_address.is)));

      c = row.createCell(10);
      c.setCellStyle(cellStyle);
      c.setCellValue(createHelper.createRichTextString(i.phone_nos.is));

      c = row.createCell(11);
      c.setCellStyle(cellStyle);
      c.setCellValue(createHelper.createRichTextString(cleanMe(i.summary.is)));

      c = row.createCell(12);
      c.setCellStyle(cellStyle);
      c.setCellValue(createHelper.createRichTextString(cleanMe(i.specialities.is)));

      c = row.createCell(13);
      c.setCellStyle(cellStyle);
      c.setCellValue(createHelper.createRichTextString(i.company_with_position.is));

      cnt = cnt + 1
    }
 try {

      var fileOut: FileOutputStream = new FileOutputStream("/var/www/html/reports/" + path);
      wb.write(fileOut);
      fileOut.close();
    } catch {
      case e: IOException =>
        logger.error(e.getMessage)
        e.printStackTrace()
      case e: Exception =>
        logger.error(e.getMessage)
        e.printStackTrace()
    }
  }
}
