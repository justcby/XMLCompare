package EDICompare.ICS;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class CompareXML {

	public static String[] whiteList = new String[] { "MSG_CTRL_REF_NUM",
			"MSG_SENT_DATETIME", "MSG_REC_DATETIME", "app_int_ref_num",
			"time_stamp", "RECORD_TIME", "record_datetime" };

	public static String PBL_EDI_NEW_FILE_PATH = "D:\\PBLEDI\\NEW\\";

	public static void main(String[] args) throws Exception {

		List<String> newFilePathList = getFilesByParentPath(
				PBL_EDI_NEW_FILE_PATH, false);
		System.out.println(newFilePathList);
		int total = newFilePathList.size();
		int checkOk = 0;
		// String filePath = "src/test/PBLEDI2.xml";
		// String filePath2 = "src/test/PBLEDI3.xml";

		SAXReader reader = new SAXReader();
		for (String newFilePath : newFilePathList) {
			// 读取文件 转换成Document
			Document newDoc = reader.read(new File(newFilePath));
			String oldFilePath = newFilePath.replaceAll("NEW", "OLD");
			Document oldDoc = reader.read(new File(oldFilePath));

			List<String> newNodeList = new ArrayList<String>();
			List<String> newTextList = new ArrayList<String>();
			List<String> oldNodeList = new ArrayList<String>();
			List<String> oldTextList = new ArrayList<String>();

			listNodes(newDoc.getRootElement(), newNodeList, newTextList);
			listNodes(oldDoc.getRootElement(), oldNodeList, oldTextList);
			// testCompare(filePath);
			// testCompare(filePath2);

			boolean isSimilarSchema = true;
			if (newNodeList.size() != oldNodeList.size()
					|| newTextList.size() != oldTextList.size()) {
				isSimilarSchema = false;
			}
			List<Integer> comparedFailedNodesList = new ArrayList<Integer>();
			List<Integer> comparedFailedTextsList = new ArrayList<Integer>();
			if (isSimilarSchema) {
				comparedFailedNodesList = compareNodesAndTexts(oldNodeList,
						newNodeList);
				comparedFailedTextsList = compareNodesAndTexts(oldTextList,
						newTextList);

			}
			if (CollectionUtils.isEmpty(comparedFailedNodesList)
					&& CollectionUtils.isEmpty(comparedFailedTextsList)
					&& isSimilarSchema) {
				checkOk++;
				System.out.println("****** matched success.******");
				System.out.println(checkOk);
			} else {
				// matched failed
				System.out.println("******matched failed.");
				for (int i = 0; i < comparedFailedTextsList.size(); i++) {
					int j = comparedFailedTextsList.get(i);
					System.out.println(newNodeList.get(j) + ":  "
							+ newTextList.get(j));
				}

				// if (CollectionUtils.isNotEmpty(comparedFailedNodesList)) {
				// System.out.println("******Failed Nodes: "
				// + StringUtils.join(comparedFailedNodesList, ", "));
				// }
				// if (CollectionUtils.isNotEmpty(comparedFailedTextsList)) {
				// System.out.println("******Failed Texts: \n"
				// + StringUtils.join(comparedFailedTextsList, "\n "));
				// }
				if (!isSimilarSchema) {
					System.out.println("******Schema is not Similar");
				}
			}

			// writeStringToFile(PBL_EDI_TEMP_FILE_PATH + pblNum + "_old.xml",
			// oldString,
			// DEFAULT_UNICODE);
			// writeStringToFile(PBL_EDI_TEMP_FILE_PATH + pblNum + "_new.xml",
			// newString,
			// DEFAULT_UNICODE);//TODO EDEN
		}
		System.out.println("**********************************");
	}

	// 遍历当前节点下的所有节点
	public static void listNodes(Element node, List<String> nodeList,
			List<String> textList) {
		nodeList.add(node.getName());
		if (!(node.getTextTrim().equals(""))
				&& !(Arrays.asList(whiteList).contains(node.getName()))) {
			textList.add(node.getText());
		} else {
			textList.add("empty");
		}

		// 当前节点下面子节点迭代器
		Iterator<Element> it = node.elementIterator();
		// 遍历
		while (it.hasNext()) {
			// 获取某个子节点对象
			Element e = it.next();
			// 对子节点进行遍历
			listNodes(e, nodeList, textList);
		}
	}

	public static List<Integer> compareNodesAndTexts(List<String> oldList,
			List<String> newList) {
		List<String> comparedFailedList = new ArrayList<String>();
		List<Integer> indexOfFailedList = new ArrayList<Integer>();
		if (CollectionUtils.isNotEmpty(newList)
				&& CollectionUtils.isNotEmpty(oldList)) {
			for (int i = 0; i < oldList.size(); i++) {
				if (oldList.get(i).equals(newList.get(i))) {
					continue;
				} else {
					comparedFailedList.add(oldList.get(i));
					indexOfFailedList.add(i);
				}
				;
			}
		}

		// return comparedFailedList;
		return indexOfFailedList;
	}

	public static List<String> getFilesByParentPath(String strPath,
			boolean isRecursion) {
		List<String> filelist = new ArrayList<String>();
		File dir = new File(strPath);
		File[] files = dir.listFiles();
		if (files == null) {
			return new ArrayList<String>();
		}
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory()) {
				if (isRecursion) {
					getFilesByParentPath(files[i].getAbsolutePath(),
							isRecursion);
				}
			} else {
				filelist.add(files[i].getAbsolutePath());
			}
		}
		return filelist;
	};

	public static String getPblNumByFilePath(String filePath) {
		String fileName = filePath.substring(filePath.lastIndexOf("\\") + 1,
				filePath.lastIndexOf("_"));
		fileName = fileName.toUpperCase();
		return fileName;
	}

}