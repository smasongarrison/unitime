/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.timetable.gwt.client.sectioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unitime.timetable.gwt.client.aria.AriaSuggestBox;
import org.unitime.timetable.gwt.client.events.UniTimeFilterBox;
import org.unitime.timetable.gwt.client.widgets.FilterBox;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Suggestion;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.StudentSectioningConstants;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.regexp.shared.MatchResult;
import com.google.gwt.regexp.shared.RegExp;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.TextBox;

/**
 * @author Tomas Muller
 */
public class SectioningStatusFilterBox extends UniTimeFilterBox<SectioningStatusFilterBox.SectioningStatusFilterRpcRequest> {
	private static final StudentSectioningMessages MESSAGES = GWT.create(StudentSectioningMessages.class);
	private static final StudentSectioningConstants CONSTANTS = GWT.create(StudentSectioningConstants.class);
	private static final GwtMessages GWT_MESSAGES = GWT.create(GwtMessages.class);
	
	private boolean iOnline;
	
	private AriaSuggestBox iCourse;
	private Chip iLastCourse;
	
	private AriaSuggestBox iStudent;
	private Chip iLastStudent;
	
	public SectioningStatusFilterBox(boolean online) {
		super(null);
		
		iOnline = online;

		FilterBox.StaticSimpleFilter mode = new FilterBox.StaticSimpleFilter("mode", GWT_MESSAGES.tagSectioningMode());
		mode.setMultipleSelection(false);
		addFilter(mode);
		
		addFilter(new FilterBox.StaticSimpleFilter("type", GWT_MESSAGES.tagSectioningType()));
		
		addFilter(new FilterBox.StaticSimpleFilter("status", GWT_MESSAGES.tagSectioningStatus()));
		addFilter(new FilterBox.StaticSimpleFilter("approver", GWT_MESSAGES.tagApprover()));

		
		addFilter(new FilterBox.StaticSimpleFilter("area", GWT_MESSAGES.tagAcademicArea()));
		addFilter(new FilterBox.StaticSimpleFilter("major", GWT_MESSAGES.tagMajor()));
		addFilter(new FilterBox.StaticSimpleFilter("concentration", GWT_MESSAGES.tagConcentration()));
		addFilter(new FilterBox.StaticSimpleFilter("minor", GWT_MESSAGES.tagMinor()));
		addFilter(new FilterBox.StaticSimpleFilter("classification", GWT_MESSAGES.tagClassification()));
		addFilter(new FilterBox.StaticSimpleFilter("degree", GWT_MESSAGES.tagDegree()));
		addFilter(new FilterBox.StaticSimpleFilter("group", GWT_MESSAGES.tagStudentGroup()));
		addFilter(new FilterBox.StaticSimpleFilter("accommodation", GWT_MESSAGES.tagStudentAccommodation()));
		addFilter(new FilterBox.StaticSimpleFilter("credit", GWT_MESSAGES.tagCredit()));
		addFilter(new FilterBox.StaticSimpleFilter("overlap", GWT_MESSAGES.tagOverlap()));
		addFilter(new FilterBox.StaticSimpleFilter("advisor", GWT_MESSAGES.tagAdvisor()));
		FilterBox.StaticSimpleFilter override = new FilterBox.StaticSimpleFilter("override", MESSAGES.tagOverride());
		override.setMultipleSelection(false);
		addFilter(override);
		
		addFilter(new FilterBox.StaticSimpleFilter("assignment", GWT_MESSAGES.tagSectioningAssignment()) {
			@Override
			public void validate(String text, AsyncCallback<Chip> callback) {
				String translatedValue = null;
				if ("assigned".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.assignmentType()[0];
				else if ("reserved".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.assignmentType()[1];
				else if ("not assigned".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.assignmentType()[2];
				else if ("wait-listed".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.assignmentType()[3];
				else if ("critical".equalsIgnoreCase(text))
					translatedValue = (CONSTANTS.assignmentType().length > 4 ? CONSTANTS.assignmentType()[4] : null);
				else if ("assigned critical".equalsIgnoreCase(text))
					translatedValue = (CONSTANTS.assignmentType().length > 5 ? CONSTANTS.assignmentType()[5] : null);
				else if ("not assigned critical".equalsIgnoreCase(text))
					translatedValue = (CONSTANTS.assignmentType().length > 6 ? CONSTANTS.assignmentType()[6] : null);
				else if ("important".equalsIgnoreCase(text))
					translatedValue = (CONSTANTS.assignmentType().length > 7 ? CONSTANTS.assignmentType()[7] : null);
				else if ("assigned important".equalsIgnoreCase(text))
					translatedValue = (CONSTANTS.assignmentType().length > 8 ? CONSTANTS.assignmentType()[8] : null);
				else if ("not assigned important".equalsIgnoreCase(text))
					translatedValue = (CONSTANTS.assignmentType().length > 9 ? CONSTANTS.assignmentType()[9] : null);
				callback.onSuccess(new Chip(getCommand(), text).withTranslatedCommand(getLabel()).withTranslatedValue(translatedValue));
			}
		});
		
		addFilter(new FilterBox.StaticSimpleFilter("consent", GWT_MESSAGES.tagSectioningConsent()) {
			@Override
			public void validate(String text, AsyncCallback<Chip> callback) {
				String translatedValue = null;
				if ("consent".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.consentTypeAbbv()[0];
				else if ("no consent".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.consentTypeAbbv()[1];
				else if ("waiting".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.consentTypeAbbv()[2];
				else if ("approved".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.consentTypeAbbv()[3];
				else if ("to do".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.consentTypeAbbv()[3];
				callback.onSuccess(new Chip(getCommand(), text).withTranslatedCommand(getLabel()).withTranslatedValue(translatedValue));
			}
		});
		
		FilterBox.StaticSimpleFilter op = new FilterBox.StaticSimpleFilter("operation", GWT_MESSAGES.tagSectioningOperation());
		op.setMultipleSelection(true);
		addFilter(op);

		final TextBox curriculum = new TextBox();
		curriculum.setStyleName("unitime-TextArea");
		curriculum.setMaxLength(100); curriculum.setWidth("200px");
		
		curriculum.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				boolean removed = removeChip(new Chip("curriculum", null), false);
				if (curriculum.getText().isEmpty()) {
					if (removed)
						fireValueChangeEvent();
				} else {
					addChip(new Chip("curriculum", curriculum.getText()).withTranslatedCommand(GWT_MESSAGES.tagCurriculum()), true);
				}
			}
		});
		
		Label courseLab = new Label(MESSAGES.propCourse());
		iCourse = new AriaSuggestBox(new CourseOracle());
		iCourse.setStyleName("unitime-TextArea");
		iCourse.setWidth("200px");
		FilterBox.StaticSimpleFilter courseFilter = new FilterBox.StaticSimpleFilter("course", GWT_MESSAGES.tagCourse());
		courseFilter.setMultipleSelection(true);
		addFilter(courseFilter);
		iCourse.getValueBox().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				courseChanged(true);
			}
		});
		iCourse.getValueBox().addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						courseChanged(false);
					}
				});
			}
		});
		iCourse.getValueBox().addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE)
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							courseChanged(false);
						}
					});
			}
		});
		iCourse.getValueBox().addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				courseChanged(true);
			}
		});
		iCourse.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<com.google.gwt.user.client.ui.SuggestOracle.Suggestion> event) {
				courseChanged(true);
			}
		});
		
		
		Label studentLab = new Label(MESSAGES.propStudent());
		studentLab.getElement().getStyle().setMarginLeft(10, Unit.PX);
		iStudent = new AriaSuggestBox(new StudentOracle());
		iStudent.setStyleName("unitime-TextArea");
		iStudent.setWidth("200px");
		addFilter(new FilterBox.StaticSimpleFilter("student", GWT_MESSAGES.tagStudent()));
		iStudent.getValueBox().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				studentChanged(true);
			}
		});
		iStudent.getValueBox().addKeyPressHandler(new KeyPressHandler() {
			@Override
			public void onKeyPress(KeyPressEvent event) {
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						studentChanged(false);
					}
				});
			}
		});
		iStudent.getValueBox().addKeyUpHandler(new KeyUpHandler() {
			@Override
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_BACKSPACE)
					Scheduler.get().scheduleDeferred(new ScheduledCommand() {
						@Override
						public void execute() {
							studentChanged(false);
						}
					});
			}
		});
		iStudent.getValueBox().addBlurHandler(new BlurHandler() {
			@Override
			public void onBlur(BlurEvent event) {
				studentChanged(true);
			}
		});
		iStudent.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {
			@Override
			public void onSelection(SelectionEvent<com.google.gwt.user.client.ui.SuggestOracle.Suggestion> event) {
				studentChanged(true);
			}
		});
		
		FilterBox.StaticSimpleFilter pref = new FilterBox.StaticSimpleFilter("prefer", GWT_MESSAGES.tagPrefer());
		pref.setVisible(false); 
		addFilter(pref);
		
		FilterBox.StaticSimpleFilter req = new FilterBox.StaticSimpleFilter("require", GWT_MESSAGES.tagRequire());
		req.setVisible(false); 
		addFilter(req);
		
		FilterBox.StaticSimpleFilter im = new FilterBox.StaticSimpleFilter("im", GWT_MESSAGES.tagInstructionalMethod());
		im.setMultipleSelection(true);
		addFilter(im);
		
		addFilter(new FilterBox.StaticSimpleFilter("lookup", GWT_MESSAGES.tagLookup()));
		
		addFilter(new FilterBox.CustomFilter("Other", GWT_MESSAGES.tagOther(), courseLab, iCourse, studentLab, iStudent) {
			@Override
			public void getSuggestions(final List<Chip> chips, final String text, AsyncCallback<Collection<FilterBox.Suggestion>> callback) {
				if (text.isEmpty()) {
					callback.onSuccess(null);
				} else {
					List<FilterBox.Suggestion> suggestions = new ArrayList<FilterBox.Suggestion>();
					Chip old = null;
					for (Chip c: chips) { if (c.getCommand().equals("limit")) { old = c; break; } }
					try {
						if (Integer.parseInt(text) <= 9999)
							suggestions.add(new Suggestion(new Chip("limit", text).withTranslatedCommand(GWT_MESSAGES.tagLimit()), old));
					} catch (NumberFormatException e) {}
					
					old = null;
					for (Chip c: chips) { if (c.getCommand().equals("credit")) { old = c; break; } }
					try {
						String number = text;
						String prefix = "";
						if (text.startsWith("<=") || text.startsWith(">=")) { number = number.substring(2); prefix = text.substring(0, 2); }
						else if (text.startsWith("<") || text.startsWith(">")) { number = number.substring(1); prefix = text.substring(0, 1); }
						try {
						if (Float.parseFloat(number) <= 99) {
							suggestions.add(new Suggestion(new Chip("credit", text).withTranslatedCommand(GWT_MESSAGES.tagCredit()), old));
							if (prefix.isEmpty()) {
								suggestions.add(new Suggestion(new Chip("credit", "<=" + text).withTranslatedCommand(GWT_MESSAGES.tagCredit()), old));
								suggestions.add(new Suggestion(new Chip("credit", ">=" + text).withTranslatedCommand(GWT_MESSAGES.tagCredit()), old));
							}
						}
						} catch (NumberFormatException e) {
							RegExp rx = RegExp.compile("^([0-9]+\\.?[0-9]*)([^0-9\\.].*)$");
							MatchResult m = rx.exec(number);
							if (m != null) {
								Float.parseFloat(m.getGroup(1));
								String im = m.getGroup(2).trim();
								suggestions.add(new Suggestion(new Chip("credit", prefix + m.getGroup(1) + " " + im).withTranslatedCommand(GWT_MESSAGES.tagCredit()), old));
								if (prefix.isEmpty()) {
									suggestions.add(new Suggestion(new Chip("credit", "<=" + m.getGroup(1) + " " + im).withTranslatedCommand(GWT_MESSAGES.tagCredit()), old));
									suggestions.add(new Suggestion(new Chip("credit", ">=" + m.getGroup(1) + " " + im).withTranslatedCommand(GWT_MESSAGES.tagCredit()), old));
								}
							}
						}
					} catch (Exception e) {}
					try {
						if (text.contains("..")) {
							try {
								String first = text.substring(0, text.indexOf('.'));
								String second = text.substring(text.indexOf("..") + 2);
								if (Float.parseFloat(first) < Float.parseFloat(second) &&  Float.parseFloat(second) <= 99) {
									suggestions.add(new Suggestion(new Chip("credit", text).withTranslatedCommand(GWT_MESSAGES.tagCredit()), old));
								}
							} catch (NumberFormatException e) {
								RegExp rx = RegExp.compile("^([0-9]+\\.?[0-9]*)\\.\\.([0-9]+\\.?[0-9]*)([^0-9].*)$");
								MatchResult m = rx.exec(text);
								if (m != null) {
									suggestions.add(new Suggestion(new Chip("credit", m.getGroup(1) + ".." + m.getGroup(2) + " " + m.getGroup(3).trim()).withTranslatedCommand(GWT_MESSAGES.tagCredit()), old));
								}
							}
						}
					} catch (Exception e) {}
					
					old = null;
					for (Chip c: chips) { if (c.getCommand().equals("overlap")) { old = c; break; } }
					try {
						String number = text;
						String prefix = "";
						if (text.startsWith("<=") || text.startsWith(">=")) { number = number.substring(2); prefix = text.substring(0, 2); }
						else if (text.startsWith("<") || text.startsWith(">")) { number = number.substring(1); prefix = text.substring(0, 1); }
						if (Integer.parseInt(number) <= 999) {
							suggestions.add(new Suggestion(new Chip("overlap", text).withTranslatedCommand(GWT_MESSAGES.tagOverlap()), old));
							if (prefix.isEmpty()) {
								suggestions.add(new Suggestion(new Chip("overlap", "<=" + text).withTranslatedCommand(GWT_MESSAGES.tagOverlap()), old));
								suggestions.add(new Suggestion(new Chip("overlap", ">=" + text).withTranslatedCommand(GWT_MESSAGES.tagOverlap()), old));
							}
						}
					} catch (Exception e) {}
					if (text.contains("..")) {
						try {
							String first = text.substring(0, text.indexOf('.'));
							String second = text.substring(text.indexOf("..") + 2);
							if (Integer.parseInt(first) < Integer.parseInt(second) &&  Integer.parseInt(second) <= 999) {
								suggestions.add(new Suggestion(new Chip("overlap", text).withTranslatedCommand(GWT_MESSAGES.tagOverlap()), old));
							}
						} catch (Exception e) {}
					}
					
					callback.onSuccess(suggestions);
				}
			}
		});
		
		addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				List<Chip> courses = getChips("course");
				iLastCourse = (courses.isEmpty() ? null : courses.get(courses.size() - 1));
				iLastStudent = getChip("student");

				if (!isFilterPopupShowing()) {
					Chip chip = getChip("curriculum");
					if (chip == null)
						curriculum.setText("");
					else
						curriculum.setText(chip.getValue());

					if (iLastCourse == null)
						iCourse.setText("");
					else
						iCourse.setText(iLastCourse.getValue());

					Chip student = getChip("student");
					if (student == null)
						iStudent.setText("");
					else
						iStudent.setText(student.getValue());
				}
				
				init(false, getAcademicSessionId(), new Command() {
					@Override
					public void execute() {
						if (isFilterPopupShowing())
							showFilterPopup();
					}
				});
			}
		});
	}
	
	private void courseChanged(boolean fireChange) {
		List<Chip> oldChips = getChips("course");
		Chip oldChip = (oldChips.isEmpty() ? null : oldChips.get(oldChips.size() - 1));
		if (iCourse.getText().isEmpty()) {
			if (oldChip != null)
				removeChip(oldChip, fireChange);
		} else {
			Chip newChip = new Chip("course", iCourse.getText()).withTranslatedCommand(GWT_MESSAGES.tagCourse());
			if (oldChip != null) {
				if (newChip.equals(oldChip)) {
					if (fireChange && !newChip.equals(iLastCourse)) fireValueChangeEvent();
					return;
				}
				removeChip(oldChip, false);
			}
			addChip(newChip, fireChange);
		}
	}
	
	private void studentChanged(boolean fireChange) {
		Chip oldChip = getChip("student");
		if (iStudent.getText().isEmpty()) {
			if (oldChip != null)
				removeChip(oldChip, fireChange);
		} else {
			Chip newChip = new Chip("student", iStudent.getText()).withTranslatedCommand(GWT_MESSAGES.tagStudent());
			if (oldChip != null) {
				if (newChip.equals(oldChip)) {
					if (fireChange && !newChip.equals(iLastStudent)) fireValueChangeEvent();
					return;
				}
				removeChip(oldChip, false);
			}
			addChip(newChip, fireChange);
		}
	}
	
	@Override
	protected boolean populateFilter(FilterBox.Filter filter, List<FilterRpcResponse.Entity> entities) {
		if (filter != null && filter instanceof FilterBox.StaticSimpleFilter) {
			FilterBox.StaticSimpleFilter simple = (FilterBox.StaticSimpleFilter)filter;
			List<FilterBox.Chip> chips = new ArrayList<FilterBox.Chip>();
			if (entities != null) {
				for (FilterRpcResponse.Entity entity: entities)
					chips.add(new FilterBox.Chip(filter.getCommand(), entity.getAbbreviation())
							.withLabel(entity.getName())
							.withCount(entity.getCount())
							.withTranslatedCommand(filter.getLabel())
							.withTranslatedValue(entity.getProperty("translated-value", null)));
			}
			simple.setValues(chips);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public SectioningStatusFilterRpcRequest createRpcRequest() {
		SectioningStatusFilterRpcRequest req = new SectioningStatusFilterRpcRequest();
		req.setOption("online", iOnline ? "true" : "false");
		return req;
	}
	
	public static class SectioningStatusFilterRpcRequest extends FilterRpcRequest {
		private static final long serialVersionUID = 1L;

		public SectioningStatusFilterRpcRequest() {}
	}
	
	public class CourseSuggestion implements SuggestOracle.Suggestion {
		private FilterBox.Suggestion iSuggestion;
		
		CourseSuggestion(FilterBox.Suggestion suggestion) {
			iSuggestion = suggestion;
		}

		@Override
		public String getDisplayString() {
			return iSuggestion.getChipToAdd().getLabel() + (iSuggestion.getChipToAdd().hasToolTip() ? " <span class='item-hint'>" + iSuggestion.getChipToAdd().getToolTip() + "</span>" : "");
		}

		@Override
		public String getReplacementString() {
			return iSuggestion.getChipToAdd().getValue();
		}
	}
	
	public class CourseOracle extends SuggestOracle {

		@Override
		public void requestSuggestions(final Request request, final Callback callback) {
			if (!request.getQuery().isEmpty()) {
				iFilter.getWidget().getSuggestionsProvider().getSuggestions(iFilter.getWidget().getChips(null), request.getQuery(), new AsyncCallback<Collection<FilterBox.Suggestion>>() {

					@Override
					public void onFailure(Throwable caught) {
					}

					@Override
					public void onSuccess(Collection<FilterBox.Suggestion> result) {
						if (result == null) return;
						List<CourseSuggestion> suggestions = new ArrayList<CourseSuggestion>();
						for (FilterBox.Suggestion suggestion: result) {
							if (suggestion.getChipToAdd() != null && "course".equals(suggestion.getChipToAdd().getCommand())) {
								suggestions.add(new CourseSuggestion(suggestion));
							}
						}
						callback.onSuggestionsReady(request, new Response(suggestions));
					}
				});
			}
		}
		
		@Override
		public boolean isDisplayStringHTML() {
			return true;
		}
	}
	
	public class StudentSuggestion implements SuggestOracle.Suggestion {
		private FilterBox.Suggestion iSuggestion;
		
		StudentSuggestion(FilterBox.Suggestion suggestion) {
			iSuggestion = suggestion;
		}

		@Override
		public String getDisplayString() {
			return iSuggestion.getChipToAdd().getLabel();
		}

		@Override
		public String getReplacementString() {
			return iSuggestion.getChipToAdd().getValue();
		}
	}
	
	public class StudentOracle extends SuggestOracle {

		@Override
		public void requestSuggestions(final Request request, final Callback callback) {
			if (!request.getQuery().isEmpty()) {
				iFilter.getWidget().getSuggestionsProvider().getSuggestions(iFilter.getWidget().getChips(null), request.getQuery(), new AsyncCallback<Collection<FilterBox.Suggestion>>() {

					@Override
					public void onFailure(Throwable caught) {
					}

					@Override
					public void onSuccess(Collection<FilterBox.Suggestion> result) {
						if (result == null) return;
						List<StudentSuggestion> suggestions = new ArrayList<StudentSuggestion>();
						for (FilterBox.Suggestion suggestion: result) {
							if (suggestion.getChipToAdd() != null && "student".equals(suggestion.getChipToAdd().getCommand())) {
								suggestions.add(new StudentSuggestion(suggestion));
							}
						}
						callback.onSuggestionsReady(request, new Response(suggestions));
					}
				});
			}
		}
	}
	
	@Override
	protected void onLoad(FilterRpcResponse result) {
		if (!result.hasEntities()) return;
		boolean added = false;
		types: for (String type: result.getTypes()) {
			for (FilterBox.Filter filter: iFilter.getWidget().getFilters()) {
				if (filter.getCommand().equals(type)) continue types;
			}
			iFilter.getWidget().getFilters().add(iFilter.getWidget().getFilters().size() - 10, new FilterBox.StaticSimpleFilter(type, result.getTypeLabel(type)));
			added = true;
		}
		if (added) setValue(getValue(), false);
	}

}
