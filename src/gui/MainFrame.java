package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDesktopPane;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import composer.ComposeInformation;
import composer.GradedCandidate;
import composer.PoemComposer;
import entities.Collocation;
import entities.PatternInformation;
import entities.Poem;

import managers.PatternManager;
import managers.PoemManager;
import managers.TonalManager;

/**
 * the main GUI frame
 * 
 * @author wei.he
 * 
 */
public class MainFrame extends JFrame {

    /**
     * the current composed poem
     */
    private Poem composedPoem = null;
    /**
     * the current pattern requirement
     */
    private PatternInformation pattern = null;
    /**
     * the current compose information
     */
    private ComposeInformation composeInfo = null;
    /**
     * the current candidates for substitution
     */
    private List<GradedCandidate> substitutionCandidates = null;

    /**
     * checkboxs for displaying options
     */
    private JCheckBox showPingze, showYunjiao, showCollocation, showSource,
	    showChange;
    /**
     * the editor pane for displaying poem
     */
    private JEditorPane editorPane;
    /**
     * the current line index to be substituted
     */
    private int currentSubstLineIndex = -1;

    /**
     * the font size for displaying a poem
     */
    private static int POEM_TEXT_FONT_SIZE = 18;
    /**
     * the font size for displaying the link to manual change
     */
    private static int POEM_CHANGE_FONT_SIZE = 9;
    /**
     * the default frame width
     */
    private static int DEFAULT_WIDTH = 500;
    /**
     * the default frame height
     */
    private static int DEFAULT_HEIGHT = 700;
    /**
     * the default width for the inner frame to display candidates for
     * substitution
     */
    private static int SUBSTITUTION_FRAME_WIDTH = 200;
    /**
     * the default heigth for the innner frame to display candidates for
     * substitution
     */
    private static int SUBSTITUTION_FRAME_HEIGHT = 400;

    /**
     * convert a color to its hexadecimal representation
     * 
     * @param c
     *            color
     * @return base 16 representation
     */
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

    /**
     * get html text for displaying a character
     * 
     * @param ch
     *            character
     * @param color
     *            color
     * @param underline
     *            whether underlined
     * @param highlight
     *            whether highlighted
     * @return html text
     */
    private String getCharHtmlText(char ch, Color color, boolean underline,
	    boolean highlight) {
	String style = "color:" + toHexStr(color) + ";font-size:"
		+ POEM_TEXT_FONT_SIZE + "px;"
		+ (highlight ? "background-color:#FFE500;" : "");
	String text = "<span style=\"" + style + "\">" + ch + "</span>";
	if (underline)
	    return "<u>" + text + "</u>";
	else
	    return text;
    }

    /**
     * get html text for displaying a line in poem
     * 
     * @param rowIndex
     *            the row index in poem
     * @param line
     *            line
     * @param showPingze
     *            whether show pingze information
     * @param showCollocation
     *            whether show collocation pairs
     * @param showYunjiao
     *            whether show yunjiaos
     * @return html text
     */
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
		    // use red color in ze, and blue for ping
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

    /**
     * get html text for the manual change link
     * 
     * @param url
     *            the url linked to
     * @return html text
     */
    private String getChangeHtmlText(String url) {
	return "<a href=\"" + url + "\"><span style=\"font-size:"
		+ POEM_CHANGE_FONT_SIZE + "px;\">(修改)</span></a>";
    }

    /**
     * get html text for displaying a poem
     * 
     * @param poem
     *            poem
     * @param showPingze
     *            show pingze information
     * @param showCollocation
     *            show collocation pairs
     * @param showYunjiao
     *            show yunjiaos
     * @param showSource
     *            show sources
     * @param showChange
     *            show manual change links
     * @return html text
     */
    private String getPoemHtmlText(Poem poem, boolean showPingze,
	    boolean showCollocation, boolean showYunjiao, boolean showSource,
	    boolean showChange) {
	String text = "<html>\n";
	text += "<body>\n";
	text += "<h1>集句结果(" + composeInfo.yunbu + ")</h1>";
	int rowCnt = poem.content.length / 2;

	for (int i = 0; i < rowCnt; i++) {
	    text += "<p>\n";
	    text += getLineHtmlText(2 * i, poem.content[2 * i], showPingze,
		    showCollocation, showYunjiao);
	    if (showChange)
		text += getChangeHtmlText("http://" + String.valueOf(2 * i)
			+ ".c");
	    text += getCharHtmlText(',', Color.black, false, false);
	    text += getLineHtmlText(2 * i + 1, poem.content[2 * i + 1],
		    showPingze, showCollocation, showYunjiao);
	    if (showChange)
		text += getChangeHtmlText("http://" + String.valueOf(2 * i + 1)
			+ ".c");
	    text += "</p>\n";
	}
	if (showSource) {
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

    /**
     * get html text for displaying candidates for substitution
     * 
     * @return html text
     */
    private String getSubstitutionHtmlText() {
	String text = "<html>\n";
	text += "<body>\n";
	if (substitutionCandidates == null || substitutionCandidates.isEmpty())
	    text += "<p>没有可以替换该句的候选!</p>\n";
	else {
	    text += "<p>单击下面的候选以更改:</p>\n";
	    for (int i = 0; i < Math.min(10, substitutionCandidates.size()); i++) {
		text += "<p><a href=\"http://" + i + ".select\">"
			+ substitutionCandidates.get(i).candidate.line
			+ "</a></p>\n";
	    }
	}
	text += "</body>\n</html>";
	return text;
    }

    /**
     * update the poem display
     */
    private void displayPoem() {
	if (composedPoem != null && pattern != null && composeInfo != null) {
	    String html = getPoemHtmlText(composedPoem,
		    showPingze.isSelected(), showCollocation.isSelected(),
		    showYunjiao.isSelected(), showSource.isSelected(),
		    showChange.isSelected());
	    editorPane.setText(html);
	}
    }

    /**
     * constructor
     */
    public MainFrame() {
	this.setTitle("古诗词自动集句系统");

	this.setLayout(new BorderLayout());

	// the desktop has two internal frames, one is always visible for
	// displaying poem, the other is visible when necessary for displaying
	// substitution candidates
	JDesktopPane desktop = new JDesktopPane();

	// the internal frame for displaying poem
	JInternalFrame poemFrame = new JInternalFrame("集句", true, false, false,
		false);
	poemFrame.setLayout(new BorderLayout());

	editorPane = new JEditorPane();
	editorPane.setEditable(false);

	editorPane.setContentType("text/html");

	poemFrame.add(new JScrollPane(editorPane), BorderLayout.CENTER);

	// the control panel for generate options
	JPanel generateControlPanel = new JPanel();
	generateControlPanel.setLayout(new FlowLayout(FlowLayout.CENTER));

	final JComboBox typeCombo = new JComboBox();
	typeCombo.setEditable(false);

	typeCombo.addItem("七绝");
	typeCombo.addItem("五绝");
	typeCombo.addItem("七律");
	typeCombo.addItem("五律");

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

	// the control panel for display options
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
	showSource.setSelected(true);
	showSource.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		displayPoem();
	    }

	});
	displayControlPanel.add(showSource);

	showChange = new JCheckBox("手工调整");
	showChange.setSelected(false);
	showChange.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		displayPoem();
	    }

	});
	displayControlPanel.add(showChange);

	JPanel controlPanel = new JPanel();
	controlPanel.setLayout(new GridLayout(2, 1));
	controlPanel.add(generateControlPanel);
	controlPanel.add(displayControlPanel);

	poemFrame.add(controlPanel, BorderLayout.SOUTH);

	generateBtn.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		String patternName = typeCombo.getSelectedItem().toString()
			+ pingzeCombo.getSelectedItem().toString();
		pattern = PatternManager.getInstance().nameToPatternInfoMap
			.get(patternName);
		assert (pattern != null);
		composeInfo = new ComposeInformation();
		composedPoem = PoemComposer.composePoem(pattern, composeInfo);
		displayPoem();
	    }

	});

	// the internal frame for displaying substitution candidates
	final JInternalFrame substitutionFrame = new JInternalFrame("替换", true,
		true, false, false);
	substitutionFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	substitutionFrame.setLayout(new BorderLayout());

	final JEditorPane substitutionEditorPane = new JEditorPane();
	substitutionEditorPane.setEditable(false);
	substitutionEditorPane.setContentType("text/html");
	substitutionFrame.add(new JScrollPane(substitutionEditorPane),
		BorderLayout.CENTER);

	// when click the manual change link, should fetch substitution
	// candidates and pop the substitutionFrame
	editorPane.addHyperlinkListener(new HyperlinkListener() {

	    @Override
	    public void hyperlinkUpdate(HyperlinkEvent evt) {
		if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		    String url = evt.getURL().toString();
		    if (url.matches("http\\://\\d\\.c")) {
			currentSubstLineIndex = url.charAt(7) - '0';
			substitutionCandidates = PoemComposer
				.findBestReplacements(pattern, composedPoem,
					currentSubstLineIndex, composeInfo);
			substitutionEditorPane
				.setText(getSubstitutionHtmlText());
			substitutionFrame.setTitle("替换-"
				+ composedPoem.content[currentSubstLineIndex]);
			substitutionFrame.reshape(MainFrame.this.getWidth()
				- SUBSTITUTION_FRAME_WIDTH - 50,
				MainFrame.this.getHeight() - 150
					- SUBSTITUTION_FRAME_HEIGHT,
				SUBSTITUTION_FRAME_WIDTH,
				SUBSTITUTION_FRAME_HEIGHT);
			substitutionFrame.setVisible(true);
			substitutionFrame.moveToFront();
		    }
		}
	    }
	});

	// when select a candidate for substitution, should update the poem
	// display
	substitutionEditorPane.addHyperlinkListener(new HyperlinkListener() {

	    @Override
	    public void hyperlinkUpdate(HyperlinkEvent evt) {
		if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
		    String url = evt.getURL().toString();
		    if (url.matches("http\\://\\d\\.select")
			    && currentSubstLineIndex >= 0
			    && currentSubstLineIndex < composedPoem.row) {
			int candidateIndex = url.charAt(7) - '0';
			GradedCandidate bestGradedCandidate = substitutionCandidates
				.get(candidateIndex);
			composedPoem.content[currentSubstLineIndex] = bestGradedCandidate.candidate.line;
			composeInfo.srcs.put(currentSubstLineIndex,
				bestGradedCandidate.candidate.sourcePoem);
			composeInfo.collos.put(currentSubstLineIndex / 2,
				bestGradedCandidate.collo);
			displayPoem();
			substitutionFrame.setVisible(false);
		    }
		}
	    }

	});

	desktop.add(poemFrame);
	desktop.add(substitutionFrame);

	this.add(desktop, BorderLayout.CENTER);
	this.setSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	poemFrame.setVisible(true);
	try {
	    poemFrame.setMaximum(true);
	} catch (PropertyVetoException e1) {
	    poemFrame.reshape(0, 0, DEFAULT_WIDTH - 50, DEFAULT_HEIGHT - 50);
	    e1.printStackTrace();
	}
	substitutionFrame.setVisible(false);
    }
}
