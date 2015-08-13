package net.xaethos.sandbox.fragments;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

public class FormControllerTest {

    PrettyFormFragment.FormController formController;

    @Before
    public void setUp() throws Exception {
        formController = new PrettyFormFragment.FormController();
    }

    @Test
    public void emailTextAccessors() throws Exception {
        assertThat(formController.getEmailText(), nullValue());

        formController.setEmailText("asdf");
        assertThat(formController.getEmailText(), is(equalString("asdf")));
    }

    @Test
    public void emailErrorAccessors() throws Exception {
        assertThat(formController.getEmailError(), nullValue());
        assertThat(formController.isEmailValid(), is(true));

        formController.setEmailError("bad!");

        assertThat(formController.getEmailError(), is(equalString("bad!")));
        assertThat(formController.isEmailValid(), is(false));

        formController.setEmailError("");

        assertThat(formController.getEmailError(), is(nullValue()));
        assertThat(formController.isEmailValid(), is(true));
    }

    @Test
    public void emailValidations() throws Exception {
        formController.setEmailText("");

        assertThat(formController.getEmailError(), is(equalString("cannot be empty")));
        assertThat(formController.isEmailValid(), is(false));

        formController.setEmailText("a");

        assertThat(formController.isEmailValid(), is(true));
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
