(function($) {
    // Set up our namespace
    window.Reconquest = window.Reconquest || {};
    Reconquest.Labels = Reconquest.Labels || {};

    /**
     * The client-context-provider function takes in context and transforms
     * it to match the shape our template requires.
     */
    function getLabelsForPR(context) {
        console.log(context['pullRequest']);
        return {}
    }

    /* Expose the client-context-provider function */
    Reconquest.Labels.getLabelsForPR = getLabelsForPR;
}(AJS.$));
