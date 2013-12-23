package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import composer.ComposeInformation;
import composer.PoemComposer;
import entities.Collocation;
import entities.PatternInformation;
import entities.Poem;

import managers.PatternManager;
import managers.TonalManager;

public class MainFrame extends JFrame {

    private Poem composedPoem = null;
    private PatternInformation pattern = null;
    private ComposeInformation composeInfo = null;

    private JCheckBox showPingze, showYunjiao, showCollocation, showSource;
    private JEditorPane editorPane;
    private JScrollPane sp;

    private String toHexStr(Color c) {
	String r = Integer.toHexString(c.getRed());
	String g = Integer.toHexString(c.getGreen());
	String b = Integer.toHexString(c.getBlue());

	if (r.length() == 1)
	    r = "0" + r;
	if (g.length() == 1)
	    g = "0" + g;
	if (b.length() == 1)
	    b = "0" + b;

	return "#" + r + g + b;
    }

    private String getCharHtmlText(char ch, Color color, boolean underline,
	    boolean highlight) {
	String style = "color:" + toHexStr(color) + ";font-size:18px;"
		+ (highlight ? "background-color:#FFE500;" : "");
	String text = "<span style=\"" + style + "\">" + ch + "</span>";
	if (underline)
	    return "<u>" + text + "</u>";
	else
	    return text;
    }

    private String getLineHtmlText(int rowIndex, String line,
	    boolean showPingze, boolean showCollocation, boolean showYunjiao) {
	String text = "";
	for (int i = 0; i < line.length(); i++) {
	    Color color = Color.black;
	    char c = line.charAt(i);
	    if (showPingze) {
		int pzVal = -1;
		if (showPingze) {
		    int pingze = TonalManager.getInstance().getPingzeInfo(c);
		    if (pingze >= 1) {
			pzVal = pingze;
		    } else {
			if (pattern.tonals[rowIndex][i] >= 1) {
			    pzVal = pattern.tonals[rowIndex][i];
			} else
			    pzVal = 0;
		    }
		    if (pzVal == 1)
			color = Color.red;
		    else if (pzVal == 2)
			color = Color.blue;
		}
	    }
	    boolean highlight = false;
	    if (i == line.length() - 1 && showYunjiao) {
		if (pattern.ruyun[rowIndex])
		    highlight = true;
	    }
	    boolean underline = false;
	    if (showCollocation) {
		Collocation collo = composeInfo.collos.get(rowIndex / 2);
		String colloToken = (rowIndex % 2 == 0) ? collo.token
			: collo.pairToken;
		int position = line.indexOf(colloToken);
		if (i >= position && i < position + colloToken.length())
		    underline = true;
	    }
	    text += getCharHtmlText(c, color, underline, highlight);
	}
	return text;
    }

    private String getPoemHtmlText(Poem poem, boolean showPingze,
	    boolean showCollocation, boolean showYunjiao, boolean showOther) {
	String text = "<html>\n";
	text += "<body>\n";
	text += "<h1>集句结果(" + composeInfo.yunbu + ")</h1>";
	int rowCnt = poem.content.length / 2;

	for (int i = 0; i < rowCnt; i++) {
	    text += "<p>\n";
	    text += getLineHtmlText(2 * i, poem.content[2 * i], showPingze,
		    showCollocation, showYunjiao);
	    text += getCharHtmlText(',', Color.black, false, false);
	    text += getLineHtmlText(2 * i + 1, poem.content[2 * i + 1],
		    showPingze, showCollocation, showYunjiao);
	    text += "</p>\n";
	}
	if (showOther) {
	    text += "<h1>来源</h1>";
	    for (int i = 0; i < poem.row; i++) {
		Poem srcPoem = composeInfo.srcs.get(i);
		text += "<p>" + "\"" + poem.content[i] + "\"来自<em>"
			+ srcPoem.author + "</em>的《" + srcPoem.title + "》</p>";
	    }
	}
	text += "</body>\n</html>";
	return text;
    }

    private void displayPoem() {
	if (composedPoem != null && pattern != null && composeInfo != null) {
	    String html = getPoemHtmlText(composedPoem,
		    showPingze.isSelected(), showCollocation.isSelected(),
		    showYunjiao.isSelected(), showSource.isSelected());
	    editorPane.setText(html);
	}
    }

    public MainFrame() {
	this.setTitle("古诗词自动集句系统");

	this.setLayout(new BorderLayout());
	editorPane = new JEditorPane();
	editorPane.setEditable(false);

	editorPane.setContentType("text/html;charset=utf-8");
	editorPane.putClientProperty("charset", "utf-8");

	sp = new JScrollPane(editorPane);
	this.add(sp, BorderLayout.CENTER);

	JPanel generateControlPanel = new JPanel();
	generateControlPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

	final JComboBox typeCombo = new JComboBox();
	typeCombo.setEditable(false);

	typeCombo.addItem("七律");
	typeCombo.addItem("五律");
	typeCombo.addItem("七绝");
	typeCombo.addItem("五绝");

	final JComboBox pingzeCombo = new JComboBox();
	pingzeCombo.setEditable(false);

	pingzeCombo.addItem("仄起");
	pingzeCombo.addItem("平起");
	pingzeCombo.addItem("仄起入韵");
	pingzeCombo.addItem("平起入韵");

	generateControlPanel.add(new JLabel("类型:"));
	generateControlPanel.add(typeCombo);

	generateControlPanel.add(new JLabel("平仄:"));
	generateControlPanel.add(pingzeCombo);

	JButton generateBtn = new JButton("生成");
	generateControlPanel.add(generateBtn);

	JPanel displayControlPanel = new JPanel();
	displayControlPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

	displayControlPanel.add(new JLabel("显示选项:"));

	showPingze = new JCheckBox("平仄");
	showPingze.setSelected(true);
	showPingze.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		displayPoem();
	    }

	});
	displayControlPanel.add(showPingze);

	showYunjiao = new JCheckBox("韵脚");
	showYunjiao.setSelected(true);
	showYunjiao.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		displayPoem();
	    }

	});
	displayControlPanel.add(showYunjiao);

	showCollocation = new JCheckBox("搭配");
	showCollocation.setSelected(true);
	showCollocation.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		displayPoem();
	    }

	});
	displayControlPanel.add(showCollocation);

	showSource = new JCheckBox("来源");
	showSource.setSelected(false);
	showSource.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		displayPoem();
	    }

	});
	displayControlPanel.add(showSource);

	JPanel controlPanel = new JPanel();
	controlPanel.setLayout(new GridLayout(2, 1));
	controlPanel.add(generateControlPanel);
	controlPanel.add(displayControlPanel);

	this.add(controlPanel, BorderLayout.SOUTH);

	generateBtn.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		String patternName = typeCombo.getSelectedItem().toString()
			+ pingzeCombo.getSelectedItem().toString();
		pattern = PatternManager.getInstance().nameToPatternInfoMap
			.get(patternName);
		assert (pattern != null);
		composeInfo = new ComposeInformation(patternName);
		composedPoem = PoemComposer.composePoem(pattern, composeInfo);
		displayPoem();
	    }

	});
    }
}
