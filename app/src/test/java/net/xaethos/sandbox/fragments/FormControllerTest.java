package net.xaethos.sandbox.fragments;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

public class FormControllerTest {

    PrettyFormFragment.FormController formController;
    Scheduler testScheduler;

    @Before
    public void setUp() throws Exception {
        formController = new PrettyFormFragment.FormController();
        testScheduler = Schedulers.immediate();
    }

    @Test(expected = NullPointerException.class)
    public void subscribeEmailObservableNonNull() throws Exception {
        formController.setEmailTextChangeObservable(null);
    }

    @Test
    public void emailValid() throws Exception {
        Action1<CharSequence> action = mockAction();
        formController.setEmailTextChangeObservable(Observable.just("foo@example.com"));

        formController.getEmailErrorsObservable()
                .subscribeOn(testScheduler)
                .observeOn(testScheduler)
                .subscribe(action);

        verify(action, only()).call(null);
    }

    @Test
    public void emailInvalidIfEmpty() throws Exception {
        Action1<CharSequence> action = mockAction();
        formController.setEmailTextChangeObservable(Observable.just(""));

        formController.getEmailErrorsObservable()
                .subscribeOn(testScheduler)
                .observeOn(testScheduler)
                .subscribe(action);

        verify(action, only()).call("required");
    }

    @SuppressWarnings("unchecked")
    private static <T> Action1<T> mockAction() {
        return mock(Action1.class);
    }

    private static Matcher<? super CharSequence> equalString(final CharSequence text) {
        return new TypeSafeMatcher<CharSequence>() {
            @Override
            protected boolean matchesSafely(CharSequence other) {
                if (text == null) {
                    return other == null;
                } else {
                    return other != null && text.toString().equals(other.toString());
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("text equal to ").appendValue(text);
            }
        };
    }
}
