/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2013 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://developer.catrobat.org/license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.catroid.uitest.formulaeditor;

import java.util.LinkedList;
import java.util.List;

import org.catrobat.catroid.ProjectManager;
import org.catrobat.catroid.R;
import org.catrobat.catroid.content.Project;
import org.catrobat.catroid.content.Script;
import org.catrobat.catroid.content.Sprite;
import org.catrobat.catroid.content.StartScript;
import org.catrobat.catroid.content.bricks.Brick;
import org.catrobat.catroid.content.bricks.ChangeSizeByNBrick;
import org.catrobat.catroid.content.bricks.WaitBrick;
import org.catrobat.catroid.formulaeditor.Formula;
import org.catrobat.catroid.formulaeditor.FormulaEditorEditText;
import org.catrobat.catroid.formulaeditor.FormulaElement;
import org.catrobat.catroid.formulaeditor.InternFormulaParser;
import org.catrobat.catroid.formulaeditor.InternToken;
import org.catrobat.catroid.formulaeditor.InternTokenType;
import org.catrobat.catroid.ui.MainMenuActivity;
import org.catrobat.catroid.uitest.util.Reflection;
import org.catrobat.catroid.uitest.util.UiTestUtils;

import android.graphics.Rect;
import android.test.suitebuilder.annotation.Smoke;
import android.text.style.BackgroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.jayway.android.robotium.solo.Solo;

public class FormulaEditorEditTextTest extends android.test.ActivityInstrumentationTestCase2<MainMenuActivity> {

	private Project project;
	private Solo solo;
	private Sprite firstSprite;
	private Brick changeBrick;
	Script startScript1;

	private static final int X_POS_EDIT_TEXT_ID = 0;
	private static final int FORMULA_EDITOR_EDIT_TEXT_ID = 1;
	private static final int FORMULA_EDITOR_EDIT_TEXT_RID = R.id.formula_editor_edit_field;

	private int totalLinesForTheInput;

	public FormulaEditorEditTextTest() {
		super(MainMenuActivity.class);

	}

	@Override
	public void setUp() throws Exception {
		createProject(UiTestUtils.DEFAULT_TEST_PROJECT_NAME + this.getClass());
		this.solo = new Solo(getInstrumentation(), getActivity());
		UiTestUtils.getIntoScriptActivityFromMainMenu(solo);
	}

	@Override
	public void tearDown() throws Exception {
		solo.finishOpenedActivities();
		UiTestUtils.clearAllUtilTestProjects();
		super.tearDown();
		solo = null;
	}

	private void createProject(String projectName) throws InterruptedException {
		this.project = new Project(null, projectName);
		firstSprite = new Sprite("nom nom nom");
		startScript1 = new StartScript(firstSprite);
		changeBrick = new ChangeSizeByNBrick(firstSprite, 0);
		Formula longFormula = createVeryLongFormula();
		WaitBrick waitBrick = new WaitBrick(firstSprite, longFormula);
		firstSprite.addScript(startScript1);
		startScript1.addBrick(changeBrick);
		startScript1.addBrick(waitBrick);
		project.addSprite(firstSprite);

		ProjectManager.getInstance().setProject(project);
		ProjectManager.getInstance().setCurrentSprite(firstSprite);

	}

	public void setAbsoluteCursorPosition(int position) {
		((FormulaEditorEditText) solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID)).setDoNotMoveCursorOnTab(true);
		Reflection.setPrivateField(solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID), "absoluteCursorPosition", position);
		clickOnFormulaEditorEditText();
	}

	public void doubleClickOnFormulaEditorEditText() {
		Rect globalVisibleRect = new Rect();
		solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getGlobalVisibleRect(globalVisibleRect);
		solo.clickOnScreen(30, globalVisibleRect.top + 10);
		solo.drag(30, 31, globalVisibleRect.top + 10, globalVisibleRect.top, 50);
	}

	//click on edit text
	public void clickOnFormulaEditorEditText() {
		Rect globalVisibleRect = new Rect();
		solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getGlobalVisibleRect(globalVisibleRect);
		solo.clickOnScreen(30, globalVisibleRect.top + 10);
	}

	public void testSingleTapOnFunctionName() {

		solo.clickOnEditText(X_POS_EDIT_TEXT_ID);

		BackgroundColorSpan COLOR_HIGHLIGHT = (BackgroundColorSpan) Reflection.getPrivateField(
				new FormulaEditorEditText(getActivity()), "COLOR_HIGHLIGHT");

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_math));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_function_rand));

		setAbsoluteCursorPosition(2);

		assertEquals("Selection cursor not found in text, but should be", 0,
				solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText().getSpanStart(COLOR_HIGHLIGHT));
		assertEquals("Selection cursor not found in text, but should be",
				solo.getString(R.string.formula_editor_function_rand).length() + "( 0 , 1 )".length(), solo
						.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText().getSpanEnd(COLOR_HIGHLIGHT));

		assertEquals("Cursor not found in text, but should be", 2, solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID)
				.getSelectionEnd());

		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
	}

	@Smoke
	public void testDoubleTapSelection() {
		BackgroundColorSpan COLOR_HIGHLIGHT = (BackgroundColorSpan) Reflection.getPrivateField(
				new FormulaEditorEditText(getActivity()), "COLOR_HIGHLIGHT");
		solo.clickOnEditText(X_POS_EDIT_TEXT_ID);

		for (int i = 0; i < 6; i++) {
			solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_1));
		}
		assertTrue("Text not found", solo.searchText("11111", true));

		assertTrue("Selection cursor found in text, but should not be", solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID)
				.getText().getSpanStart(COLOR_HIGHLIGHT) == -1);
		setAbsoluteCursorPosition(3);
		doubleClickOnFormulaEditorEditText();
		assertEquals("Selection cursor not found in text, but should be", 0,
				solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText().getSpanStart(COLOR_HIGHLIGHT));
		assertEquals("Selection cursor not found in text, but should be", 6,
				solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText().getSpanEnd(COLOR_HIGHLIGHT));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));

		assertFalse("Text found but shouldnt", solo.searchText("11111", true));
		assertTrue("Error cursor found in text, but should not be", solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID)
				.getText().getSpanStart(COLOR_HIGHLIGHT) == -1);

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_random));
		assertTrue("Text not found", solo.searchText(solo.getString(R.string.formula_editor_function_rand) + "(", true));
		setAbsoluteCursorPosition(3);
		doubleClickOnFormulaEditorEditText();

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));

		assertFalse("Text found but shouldnt",
				solo.searchText(solo.getString(R.string.formula_editor_function_rand) + "(", true));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_sensors));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_sensor_y_acceleration));
		assertTrue("Text not found",
				solo.searchText(solo.getString(R.string.formula_editor_sensor_y_acceleration), true));
		setAbsoluteCursorPosition(3);
		doubleClickOnFormulaEditorEditText();

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));

		assertFalse("Text found but shouldnt",
				solo.searchText(solo.getString(R.string.formula_editor_sensor_y_acceleration), true));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_sensors));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_sensor_y_acceleration));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_sensors));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_sensor_x_acceleration));
		assertTrue(
				"Text not found",
				solo.searchText(
						solo.getString(R.string.formula_editor_sensor_y_acceleration) + " "
								+ solo.getString(R.string.formula_editor_sensor_x_acceleration), true));

		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
	}

	@Smoke
	public void testFunctionFirstParameterSelectionAndModification() {

		solo.clickOnEditText(X_POS_EDIT_TEXT_ID);

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_math));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_function_sin));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_1));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_2));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_decimal_mark));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_3));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_4));

		assertEquals("Function parameter modification failed", solo.getString(R.string.formula_editor_function_sin)
				+ "( 12" + getActivity().getString(R.string.formula_editor_decimal_mark) + "34 ) ",
				solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText().toString());

		setAbsoluteCursorPosition(2);

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));

		assertEquals("Text deletion was wrong!", " ", solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText()
				.toString());

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_math));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_function_rand));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_1));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_2));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_decimal_mark));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_3));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_4));

		assertEquals("Function parameter modification failed", solo.getString(R.string.formula_editor_function_rand)
				+ "( 12" + getActivity().getString(R.string.formula_editor_decimal_mark) + "34 , 1 ) ", solo
				.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText().toString());

		setAbsoluteCursorPosition(2);

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));

		assertEquals("Text deletion was wrong!", " ", solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText()
				.toString());

		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
	}

	@Smoke
	public void testBracketValueSelectionAndModification() {

		solo.clickOnEditText(X_POS_EDIT_TEXT_ID);

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_bracket_open));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_1));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_decimal_mark));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_3));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_bracket_close));

		assertEquals("Bracket value modification failed",
				"( 1" + getActivity().getString(R.string.formula_editor_decimal_mark) + "3 ) ",
				solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText().toString());
		setAbsoluteCursorPosition(1);
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		String editTextString = "1" + getActivity().getString(R.string.formula_editor_decimal_mark) + "3 "
				+ getActivity().getString(R.string.formula_editor_bracket_close) + " ";
		assertEquals("Text deletion was wrong!", editTextString, solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID)
				.getText().toString());

		setAbsoluteCursorPosition(0);
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_bracket_open));
		setAbsoluteCursorPosition(100);

		editTextString = getActivity().getString(R.string.formula_editor_bracket_open) + " 1"
				+ getActivity().getString(R.string.formula_editor_decimal_mark) + "3 ";

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		assertEquals("Text deletion was wrong!", editTextString, solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID)
				.getText().toString());

		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
	}

	@Smoke
	public void testFunctionDeletion() {

		int functionRandomLength = solo.getCurrentActivity().getText(R.string.formula_editor_function_rand).length();

		solo.clickOnEditText(X_POS_EDIT_TEXT_ID);

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_math));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_function_rand));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		assertEquals("Function deletion failed!", " ", solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText()
				.toString());

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_math));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_function_rand));

		setAbsoluteCursorPosition(functionRandomLength + 5);

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		assertEquals("Function deletion failed!", " ", solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText()
				.toString());

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_math));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_function_rand));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));

		assertEquals("Function deletion failed!", " ", solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText()
				.toString());

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_math));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_function_rand));
		setAbsoluteCursorPosition(1);
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		assertEquals("Function deletion failed!", " ", solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText()
				.toString());

		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
	}

	@Smoke
	public void testNumberInsertion() {

		solo.clickOnEditText(X_POS_EDIT_TEXT_ID);

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_decimal_mark));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_1));

		assertEquals("Number insertion failed!", "0" + getActivity().getString(R.string.formula_editor_decimal_mark)
				+ "1 ", solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText().toString());
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_decimal_mark));
		assertEquals("Delimiter insertion failed!", "0" + getActivity().getString(R.string.formula_editor_decimal_mark)
				+ "1 ", solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText().toString());

		setAbsoluteCursorPosition(1);
		doubleClickOnFormulaEditorEditText();
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));

		assertEquals("Number deletion failed!", " ", solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText().toString());

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_1));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_2));

		setAbsoluteCursorPosition(0);
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_decimal_mark));

		assertEquals("Delimiter insertion failed!", "0" + getActivity().getString(R.string.formula_editor_decimal_mark)
				+ "12 ", solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText().toString());

		setAbsoluteCursorPosition(1);
		doubleClickOnFormulaEditorEditText();
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_3));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_4));
		setAbsoluteCursorPosition(0);
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_2));
		setAbsoluteCursorPosition(0);
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_1));
		setAbsoluteCursorPosition(2);
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_decimal_mark));

		assertEquals("Delimiter insertion failed!", "12"
				+ getActivity().getString(R.string.formula_editor_decimal_mark) + "34 ",
				solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText().toString());

		setAbsoluteCursorPosition(1);
		doubleClickOnFormulaEditorEditText();
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));

		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
	}

	@Smoke
	public void testGoBackToDiscardChanges() {

		solo.clickOnEditText(X_POS_EDIT_TEXT_ID);
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_9));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_9));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_decimal_mark));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_9));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_plus));
		solo.sleep(50);
		solo.goBack();
		solo.goBack();

		assertTrue("Toast not found", solo.searchText(solo.getString(R.string.formula_editor_changes_discarded)));
		assertEquals("Wrong text in FormulaEditor", "0" + getActivity().getString(R.string.formula_editor_decimal_mark)
				+ "0 ", solo.getEditText(X_POS_EDIT_TEXT_ID).getText().toString());

		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
	}

	@Smoke
	public void testErrorInFirstAndLastCharactersAndEmptyFormula() {

		solo.clickOnEditText(X_POS_EDIT_TEXT_ID);
		BackgroundColorSpan COLOR_ERROR = (BackgroundColorSpan) Reflection.getPrivateField(new FormulaEditorEditText(
				getActivity()), "COLOR_ERROR");
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		assertTrue("Error cursor found in text, but should not be",
				solo.getEditText(1).getText().getSpanStart(COLOR_ERROR) == -1);
		solo.goBack();
		assertTrue("Toast not found", solo.searchText(solo.getString(R.string.formula_editor_parse_fail)));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_plus));
		solo.goBack();
		solo.sleep(50);
		assertTrue("Toast not found", solo.searchText(solo.getString(R.string.formula_editor_parse_fail)));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_1));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_plus));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_1));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_plus));
		solo.goBack();
		solo.sleep(50);
		assertTrue("Toast not found", solo.searchText(solo.getString(R.string.formula_editor_parse_fail)));

		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
	}

	@Smoke
	public void testTextCursorAndScrolling() {

		solo.clickOnEditText(1);

		assertTrue("Text could not be found!", solo.searchText("999999999999999999 ")); //note always ALL the text can be found by solo, not just the part currently visible due to scroll position 
		setAbsoluteCursorPosition("999999999999999999".length());
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));

		assertTrue("Wrong number of characters deleted!", solo.searchText("9999999999999999 "));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_9));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_9));
		setAbsoluteCursorPosition("9999999999999999999 + ".length() * totalLinesForTheInput - 4);

		assertTrue("Text could not be found!", solo.searchText(" 646464646464646464 "));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		assertTrue("Wrong number of characters deleted!", solo.searchText(" 6464646464646464 "));

		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
	}

	@Smoke
	public void testTextPreviewWithCursorPositions() {
		solo.clickOnEditText(1);

		EditText preview = (EditText) UiTestUtils.getViewContainerByIds(solo, R.id.brick_wait_edit_text,
				R.id.formula_editor_brick_space);
		FormulaEditorEditText formulaEditorEditText = (FormulaEditorEditText) solo
				.getView(FORMULA_EDITOR_EDIT_TEXT_RID);

		setAbsoluteCursorPosition(0);
		assertTrue("Start not visible in preview after cursor change", preview.getText().toString().contains("9"));

		setAbsoluteCursorPosition(formulaEditorEditText.getText().toString().indexOf("2 + 1") + 2);
		assertTrue("Middle not visible in preview after cursor change", preview.getText().toString().contains("2 + 1"));

		setAbsoluteCursorPosition(formulaEditorEditText.getText().length());
		assertTrue("End not visible in preview after cursor change", preview.getText().toString().contains("64"));

		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
	}

	@Smoke
	public void testParseErrorsAndDeletion() {

		String editTextString = "";

		solo.clickOnEditText(X_POS_EDIT_TEXT_ID);

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_plus));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_8));
		solo.goBack();
		solo.sleep(500);
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		assertEquals("Text not deleted correctly", "8 ", solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText()
				.toString());
		setAbsoluteCursorPosition(1);
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_8));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_plus));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_8));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_random));
		solo.goBack();
		solo.sleep(500);
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		assertEquals("Text not deleted correctly", "8 + 8 ", solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText()
				.toString());
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_8));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_plus));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_random));
		editTextString = "8 + " + getActivity().getString(R.string.formula_editor_function_rand) + "( 0 , 1 ) ";
		setAbsoluteCursorPosition(editTextString.length());
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_plus));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_plus));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_9));
		solo.goBack();
		solo.sleep(500);
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		editTextString += "+ 9 ";
		assertEquals("Text not deleted correctly", editTextString, solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID)
				.getText().toString());
		setAbsoluteCursorPosition(editTextString.length());
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_8));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_plus));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_random));
		editTextString = "8 + " + getActivity().getString(R.string.formula_editor_function_rand) + "( 0 ,";
		setAbsoluteCursorPosition(editTextString.length() + 2);
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_plus));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_2));
		editTextString += " + 2 ) ";
		setAbsoluteCursorPosition(editTextString.length());
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_3));
		solo.goBack();
		solo.sleep(500);
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		assertEquals("Text not deleted correctly", "8 + 3 ", solo.getEditText(FORMULA_EDITOR_EDIT_TEXT_ID).getText()
				.toString());

		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
	}

	public void testStrings() {

		solo.clickOnEditText(0);

		FormulaEditorEditText formulaEditorEditText = (FormulaEditorEditText) solo
				.getView(FORMULA_EDITOR_EDIT_TEXT_RID);

		String hyphen = "-";
		String costume = "costume";
		String sprite = "sprite";

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_random));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_math));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_function_abs));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_math));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_function_sin));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_math));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_function_cos));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_math));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_function_tan));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_math));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_function_ln));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_math));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_function_log));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_math));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_function_sqrt));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_math));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_function_round));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_math));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_function_pi));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_sensors));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_sensor_x_acceleration));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_sensors));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_sensor_y_acceleration));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_sensors));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_sensor_z_acceleration));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_sensors));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_sensor_z_orientation));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_sensors));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_sensor_y_orientation));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_sensors));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_sensor_x_orientation));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_object));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_look_x));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_object));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_look_y));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_object));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_look_ghosteffect));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_object));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_look_brightness));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_object));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_look_size));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_object));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_look_rotation));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_object));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_look_layer));

		String editTextString = formulaEditorEditText.getText().toString();
		boolean hyphenOrCostumephraseOrSpritephraseFound = editTextString.contains(hyphen)
				|| editTextString.contains(costume) || editTextString.contains(sprite);

		assertFalse("Unallowed char or string found (hyphen, costumephrase, spritephrase).",
				hyphenOrCostumephraseOrSpritephraseFound);

		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
	}

	public void testComputeDialog() {

		solo.clickOnEditText(0);

		View preview = UiTestUtils.getViewContainerByIds(solo, R.id.brick_change_size_by_edit_text,
				R.id.formula_editor_brick_space);

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_minus));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_2));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_compute));
		TextView computeTextView = (TextView) solo.getView(R.id.formula_editor_compute_dialog_textview);
		assertEquals("computeTextView did not contain the correct value", "-2.0", computeTextView.getText().toString());

		solo.goBack();
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_minus));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_6));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_decimal_mark));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_1));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_1));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_1));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_minus));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_compute));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_delete));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_compute));
		computeTextView = (TextView) solo.getView(R.id.formula_editor_compute_dialog_textview);
		assertEquals("computeTextView did not contain the correct value", "-8.11", computeTextView.getText().toString());

		solo.goBack();

		solo.clickOnView(preview);
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_sensors));
		solo.clickOnText(getActivity().getString(R.string.formula_editor_sensor_x_acceleration));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_plus));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_random));

		String formulaString = getActivity().getString(R.string.formula_editor_sensor_x_acceleration) + " + "
				+ solo.getString(R.string.formula_editor_function_rand) + "( 0 , 1";
		setAbsoluteCursorPosition(formulaString.length());
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_0));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_0));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_0));
		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_0));

		solo.clickOnView(solo.getView(R.id.formula_editor_keyboard_compute));
		computeTextView = (TextView) solo.getView(R.id.formula_editor_compute_dialog_textview);
		int maxLoops = 10;
		String lastComputeString = computeTextView.getText().toString();
		while (maxLoops-- > 0) {
			solo.sleep(1000);
			if (!computeTextView.getText().toString().equals(lastComputeString)) {
				break;
			}
		}

		assertTrue("Sensor interpretation error", maxLoops > 0);

		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();
		solo.goBack();

	}

	private Formula createVeryLongFormula() {

		List<InternToken> internTokenList = new LinkedList<InternToken>();

		internTokenList.add(new InternToken(InternTokenType.NUMBER, "999999999999999999"));
		internTokenList.add(new InternToken(InternTokenType.OPERATOR, "+"));
		internTokenList.add(new InternToken(InternTokenType.NUMBER, "888888888888888888"));
		//		internTokenList.add(new InternToken(InternTokenType.OPERATOR, "+"));
		//		internTokenList.add(new InternToken(InternTokenType.NUMBER, "777777777777777777"));
		//		internTokenList.add(new InternToken(InternTokenType.OPERATOR, "+"));
		//		internTokenList.add(new InternToken(InternTokenType.NUMBER, "666666666666666666"));
		//		internTokenList.add(new InternToken(InternTokenType.OPERATOR, "+"));
		//		internTokenList.add(new InternToken(InternTokenType.NUMBER, "555555555555555555"));
		//		internTokenList.add(new InternToken(InternTokenType.OPERATOR, "+"));
		//		internTokenList.add(new InternToken(InternTokenType.NUMBER, "444444444444444444"));
		//		internTokenList.add(new InternToken(InternTokenType.OPERATOR, "+"));
		//		internTokenList.add(new InternToken(InternTokenType.NUMBER, "333333333333333333"));
		//		internTokenList.add(new InternToken(InternTokenType.OPERATOR, "+"));
		//
		//		internTokenList.add(new InternToken(InternTokenType.NUMBER, "222222222222222222"));
		//		internTokenList.add(new InternToken(InternTokenType.OPERATOR, "+"));
		//		internTokenList.add(new InternToken(InternTokenType.NUMBER, "111111111111111111"));
		//		internTokenList.add(new InternToken(InternTokenType.OPERATOR, "+"));
		//		internTokenList.add(new InternToken(InternTokenType.NUMBER, "000000000000000000"));
		//		internTokenList.add(new InternToken(InternTokenType.OPERATOR, "+"));
		//		internTokenList.add(new InternToken(InternTokenType.NUMBER, "919191919191919191"));
		//		internTokenList.add(new InternToken(InternTokenType.OPERATOR, "+"));
		//		internTokenList.add(new InternToken(InternTokenType.NUMBER, "828282828282828282"));
		//		internTokenList.add(new InternToken(InternTokenType.OPERATOR, "+"));
		//		internTokenList.add(new InternToken(InternTokenType.NUMBER, "737373737373737373"));
		//		internTokenList.add(new InternToken(InternTokenType.OPERATOR, "+"));
		//		internTokenList.add(new InternToken(InternTokenType.NUMBER, "646464646464646464"));
		totalLinesForTheInput = internTokenList.size();
		InternFormulaParser internParser = new InternFormulaParser(internTokenList);
		FormulaElement root = internParser.parseFormula();
		Formula formula = new Formula(root);

		return formula;
	}
}
