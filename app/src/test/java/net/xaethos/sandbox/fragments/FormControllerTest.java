package net.xaethos.sandbox.fragments;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import rx.Observable;
import rx.Scheduler;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.verify;

@RunWith(DataProviderRunner.class)
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
    @DataProvider(value = {",required",
            "a,invalid email",
            "foo@example,invalid email",
            "foo@example.com,null",
            "foo+alt@example.com,null"
    })
    public void emailInputError(String input, String error) throws Exception {
        Action1<CharSequence> onError = subscribeMockAction(formController.emailErrors());
        emailTextInput.onNext(input);
        verify(onError, only()).call(error);
    }

    @Test
    @DataProvider(value = {",null", "some text,null", "12345678901,max 10 characters"})
    public void thingamajigInputError(String input, String error) throws Exception {
        Action1<CharSequence> onError = subscribeMockAction(formController.thingamajigErrors());
        thingamajigTextInput.onNext(input);
        verify(onError, only()).call(error);
    }

    @Test
    @DataProvider(value = {",null",
            "13,null",
            "foo,invalid number",
            "5.1,invalid number",
            "-3,must be positive"
    })
    public void fiddlesticksInputError(String input, String error) throws Exception {
        Action1<CharSequence> onError = subscribeMockAction(formController.fiddlesticksErrors());
        fiddlesticksTextInput.onNext(input);
        verify(onError, only()).call(error);
    }

    @Test
    @DataProvider(value = {"a@b.c,foo,1,true",
            "a@b.c,,,true",
            ",foo,1,false",
            "a@b,foo,1,false",
            "a@b.c,foo bar baz,1,false",
            "a@b.c,foo,1.2,false",
            "a@b.c,foo,-1,false",
    })
    public void submitEnabledWhenAllValid(
            String email, String thingamajig, String fiddlesticks, boolean submitEnabled)
            throws Exception {
        Action1<Boolean> setEnabled = subscribeMockAction(formController.submitEnabledControl());
        emailTextInput.onNext(email);
        thingamajigTextInput.onNext(thingamajig);
        fiddlesticksTextInput.onNext(fiddlesticks);
        verify(setEnabled).call(submitEnabled);
    }

    @SuppressWarnings("unchecked")
    private <T> Action1<T> subscribeMockAction(Observable<T> observable) {
        Action1<T> mockAction = mock(Action1.class);
        observable.subscribeOn(testScheduler).observeOn(testScheduler).subscribe(mockAction);
        return mockAction;
    }

}
