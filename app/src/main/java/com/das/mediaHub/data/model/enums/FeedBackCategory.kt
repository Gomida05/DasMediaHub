package com.das.mediaHub.data.model.enums

/**
 * Enum representing categories of user feedback.
 *
 * @property label The display label for the category.
 */
enum class FeedBackCategory(val label: String) {
    /** General feedback or comments. */
    General("General"),
    
    /** Reporting a technical issue or bug. */
    Bug("Bug"),
    
    /** Requesting a new feature or improvement. */
    Feature("Feature"),
    
    /** Providing positive feedback or praise. */
    Praise("Praise")
}
