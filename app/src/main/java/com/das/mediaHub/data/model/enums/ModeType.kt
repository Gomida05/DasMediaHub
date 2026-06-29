package com.das.mediaHub.data.model.enums

/**
 * Enum representing feedback ratings for the application.
 *
 * @property label The display label for the rating.
 */
enum class ModeType(val label: String) {
    /** User loves the experience. */
    LoveIt("Love it"),
    
    /** User finds the experience okay. */
    Okay("Okay"),
    
    /** User finds the feature or app broken. */
    Broken("Broken")
}
