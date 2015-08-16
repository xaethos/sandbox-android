package net.xaethos.sandbox.fragments;

import org.junit.Before;
import org.junit.Test;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

public class FormControllerTest {

    PrettyFormFragment.FormController formController;
    Scheduler testScheduler;

    PublishSubject<CharSequence> emailTextInput;
    PublishSubject<CharSequence> thingamajigTextInput;
    PublishSubject<CharSequence> fiddlesticksTextInput;

    @Before
    public void setUp() throws Exception {
        emailTextInput = PublishSubject.create();
        thingamajigTextInput = PublishSubject.create();
        fiddlesticksTextInput = PublishSubject.create();

        formController = new PrettyFormFragment.FormController(emailTextInput,
                thingamajigTextInput,
                fiddlesticksTextInput);
        testScheduler = Schedulers.immediate();
    }

    @Test
    public void emailValid() throws Exception {
        Action1<CharSequence> onError = subscribeMockAction(formController.emailErrors());
        emailTextInput.onNext("foo@example.com");
        verify(onError, only()).call(null);
    }

    @Test
    public void emailInvalidIfEmpty() throws Exception {
        Action1<CharSequence> onError = subscribeMockAction(formController.emailErrors());
        emailTextInput.onNext("");
        verify(onError, only()).call("required");
    }

    @Test
    public void thingamajigValid() throws Exception {
        Action1<CharSequence> onError = subscribeMockAction(formController.thingamajigErrors());
        thingamajigTextInput.onNext("this is a thing");
        verify(onError, only()).call(null);
    }

    @Test
    public void thingamajigInvalidIfEmpty() throws Exception {
        Action1<CharSequence> onError = subscribeMockAction(formController.thingamajigErrors());
        thingamajigTextInput.onNext("");
        verify(onError, only()).call("required");
    }

    @Test
    public void fiddlesticksValid() throws Exception {
        Action1<CharSequence> onError = subscribeMockAction(formController.fiddlesticksErrors());
        fiddlesticksTextInput.onNext("5");
        verify(onError, only()).call(null);
    }

    @Test
    public void fiddlesticksInvalidIfNotNumber() throws Exception {
        Action1<CharSequence> onError = subscribeMockAction(formController.fiddlesticksErrors());
        fiddlesticksTextInput.onNext("abc");
        verify(onError, only()).call("invalid number");
    }

    @Test
    public void fiddlesticksInvalidIfNotInteger() throws Exception {
        Action1<CharSequence> onError = subscribeMockAction(formController.fiddlesticksErrors());
        fiddlesticksTextInput.onNext("5.1");
        verify(onError, only()).call("invalid number");
    }

    @Test
    public void submitEnabledWhenEmailIsValid() throws Exception {
        Action1<Boolean> setEnabled = subscribeMockAction(formController.submitEnabledControl());
        emailTextInput.onNext("a@b.com");
        verify(setEnabled, only()).call(true);
    }

    @Test
    public void submitDisabledWhenEmailIsInvalid() throws Exception {
        Action1<Boolean> setEnabled = subscribeMockAction(formController.submitEnabledControl());
        emailTextInput.onNext("");
        verify(setEnabled, only()).call(false);
    }

    @SuppressWarnings("unchecked")
    private <T> Action1<T> subscribeMockAction(Observable<T> observable) {
        Action1<T> mockAction = mock(Action1.class);
        observable.subscribeOn(testScheduler).observeOn(testScheduler).subscribe(mockAction);
        return mockAction;
    }

}
