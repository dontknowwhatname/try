package com.example;

import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.text.rtf.RTFEditorKit;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;

public class TextEditor {
    private RSyntaxTextArea textArea;

    public TextEditor() {
        JFrame frame = new JFrame("文本编辑器");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        frame.add(sp, BorderLayout.CENTER);

        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("文件");
        addFileMenuItems(fileMenu);
        menuBar.add(fileMenu);

        JMenu searchMenu = new JMenu("搜索");
        addSearchMenuItems(searchMenu);
        menuBar.add(searchMenu);

        JMenu viewMenu = new JMenu("查看");
        addViewMenuItems(viewMenu);
        menuBar.add(viewMenu);

        JMenu helpMenu = new JMenu("帮助");
        addHelpMenuItems(helpMenu);
        menuBar.add(helpMenu);

        frame.setJMenuBar(menuBar);

        JLabel dateLabel = new JLabel("当前时间: " + LocalDateTime.now());
        frame.add(dateLabel, BorderLayout.NORTH);

        frame.setVisible(true);
    }

    private void addFileMenuItems(JMenu fileMenu) {
        JMenuItem newItem = new JMenuItem("新建");
        newItem.addActionListener(e -> new TextEditor());
        fileMenu.add(newItem);

        JMenuItem openItem = new JMenuItem("打开");
        openItem.addActionListener(this::openFile);
        fileMenu.add(openItem);

        JMenuItem saveItem = new JMenuItem("保存");
        saveItem.addActionListener(this::saveFile);
        fileMenu.add(saveItem);

        JMenuItem exitItem = new JMenuItem("退出");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        JMenuItem printItem = new JMenuItem("打印");
        printItem.addActionListener(e -> printFile());
        fileMenu.add(printItem);
    }

    private void addSearchMenuItems(JMenu searchMenu) {
        JMenuItem findItem = new JMenuItem("查找");
        findItem.addActionListener(e -> searchText());
        searchMenu.add(findItem);
    }

    private void addViewMenuItems(JMenu viewMenu) {
        JMenuItem dateTimeItem = new JMenuItem("显示当前时间");
        dateTimeItem.addActionListener(e -> JOptionPane.showMessageDialog(null, "当前时间: " + LocalDateTime.now()));
        viewMenu.add(dateTimeItem);
    }

    private void addHelpMenuItems(JMenu helpMenu) {
        JMenuItem aboutItem = new JMenuItem("关于");
        aboutItem.addActionListener(e -> JOptionPane.showMessageDialog(null, "团队成员: XXX, YYY"));
        helpMenu.add(aboutItem);
    }

    private void openFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            try {
                String content = "";
                String fileName = file.getName();
                String extension = getFileExtension(fileName);

                if (extension.equalsIgnoreCase("rtf")) {
                    content = readRtfFile(file);
                } else {
                    content = new String(Files.readAllBytes(file.toPath()));
                }
                textArea.setText(content);
                setSyntaxStyle(fileName);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null, "无法打开文件: " + ex.getMessage());
            }
        }
    }
    private void saveFile(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("文本文件", "txt"));
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF 文件", "pdf"));

        int returnValue = fileChooser.showSaveDialog(null);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String selectedFilter = fileChooser.getFileFilter().getDescription();

            if (selectedFilter.contains("PDF")) {
                if (!file.getName().toLowerCase().endsWith(".pdf")) {
                    file = new File(file.getAbsolutePath() + ".pdf");
                }

                // Save as PDF
                try (PDDocument document = new PDDocument()) {
                    PDPage page = new PDPage();
                    document.addPage(page);

                    PDPageContentStream contentStream = new PDPageContentStream(document, page);
                    contentStream.beginText();
                    contentStream.setFont(PDType1Font.HELVETICA, 12);
                    contentStream.newLineAtOffset(50, 750);

                    String[] lines = textArea.getText().split("\n");
                    for (String line : lines) {
                        contentStream.showText(line);
                        contentStream.newLineAtOffset(0, -15);
                    }

                    contentStream.endText();
                    contentStream.close();
                    document.save(file);
                    JOptionPane.showMessageDialog(null, "文件已成功保存为PDF!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "无法保存为PDF: " + ex.getMessage());
                }
            } else {
                if (!file.getName().toLowerCase().endsWith(".txt")) {
                    file = new File(file.getAbsolutePath() + ".txt");
                }

                // Save as text file
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write(textArea.getText());
                    JOptionPane.showMessageDialog(null, "文件已成功保存为文本文件!");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, "保存文件时发生错误: " + ex.getMessage());
                }
            }
        }
    }

    private void printFile() {
        try {
            boolean printed = textArea.print();
            if (!printed) {
                JOptionPane.showMessageDialog(null, "打印取消");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "打印失败: " + e.getMessage());
        }
    }

    private void searchText() {
        String searchTerm = JOptionPane.showInputDialog("输入要查找的单词:");
        if (searchTerm != null && !searchTerm.isEmpty()) {
            String content = textArea.getText();
            int index = content.indexOf(searchTerm);
            if (index != -1) {
                textArea.select(index, index + searchTerm.length());
                textArea.requestFocus();
            } else {
                JOptionPane.showMessageDialog(null, "未找到该单词");
            }
        }
    }

    private void setSyntaxStyle(String fileName) {
        String fileExtension = getFileExtension(fileName);
        switch (fileExtension) {
            case "java":
                textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
                break;
            case "py":
                textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
                break;
            case "cpp":
                textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_C);
                break;
            case "html":
                textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
                break;
            case "css":
                textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSS);
                break;
            default:
                textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
                break;
        }
    }

    private String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        return (lastIndex == -1) ? "" : fileName.substring(lastIndex + 1);
    }

    private String readRtfFile(File file) throws IOException {      //Read RTF
        StringBuilder content = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(file)) {
            RTFEditorKit rtfEditorKit = new RTFEditorKit();
            PlainDocument document = (PlainDocument) rtfEditorKit.createDefaultDocument();
            rtfEditorKit.read(fis, document, 0);
            content.append(document.getText(0, document.getLength()));
        } catch (BadLocationException ex) {
            throw new IOException("读取 RTF 文件时发生错误: " + ex.getMessage());
        }
        return content.toString();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TextEditor::new);
    }
}
