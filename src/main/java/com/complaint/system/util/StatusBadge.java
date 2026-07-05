package com.complaint.system.util;

import com.complaint.system.entity.Complaint;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * Renders complaint statuses as colored "pill" badges.
 * The pill colors live in css/styles.css under the .status-* classes.
 */
public final class StatusBadge {

    private static final String[] ALL_CLASSES = {
        "status-lodged", "status-in-progress", "status-resolved", "status-closed"
    };

    private StatusBadge() {}

    /** Maps a status value (e.g. "IN_PROGRESS") to its CSS style class. */
    public static String styleClassFor(String status) {
        if (status == null) {
            return "status-closed";
        }
        switch (status.toUpperCase()) {
            case "LODGED":      return "status-lodged";
            case "IN_PROGRESS": return "status-in-progress";
            case "RESOLVED":    return "status-resolved";
            case "CLOSED":      return "status-closed";
            default:            return "status-closed";
        }
    }

    /** Turns "IN_PROGRESS" into "IN PROGRESS" for display. */
    public static String prettify(String status) {
        return status == null ? "" : status.replace('_', ' ');
    }

    /** Styles an existing label as a colored status pill. */
    public static void apply(Label label, String status) {
        label.getStyleClass().removeAll(ALL_CLASSES);
        label.setText(prettify(status));
        label.getStyleClass().add(styleClassFor(status));
    }

    /**
     * Converter that displays a ComplaintStatus with pretty labels
     * (e.g. "IN PROGRESS") in ComboBoxes while keeping the enum as the value.
     */
    public static StringConverter<Complaint.ComplaintStatus> statusConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(Complaint.ComplaintStatus status) {
                return status == null ? "" : prettify(status.name());
            }

            @Override
            public Complaint.ComplaintStatus fromString(String display) {
                if (display == null || display.isBlank()) {
                    return null;
                }
                return Complaint.ComplaintStatus.valueOf(
                    display.trim().replace(' ', '_').toUpperCase());
            }
        };
    }

    /** Cell factory that renders a String status column as a colored pill. */
    public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> cellFactory() {
        return column -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null || status.isEmpty()) {
                    setGraphic(null);
                    setText(null);
                } else {
                    Label pill = new Label(prettify(status));
                    pill.getStyleClass().add(styleClassFor(status));
                    setGraphic(pill);
                    setText(null);
                }
            }
        };
    }
}
