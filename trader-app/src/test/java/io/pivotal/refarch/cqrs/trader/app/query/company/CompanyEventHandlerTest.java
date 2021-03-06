package io.pivotal.refarch.cqrs.trader.app.query.company;

import io.pivotal.refarch.cqrs.trader.coreapi.company.CompanyByIdQuery;
import io.pivotal.refarch.cqrs.trader.coreapi.company.CompanyCreatedEvent;
import io.pivotal.refarch.cqrs.trader.coreapi.company.CompanyId;
import io.pivotal.refarch.cqrs.trader.coreapi.company.FindAllCompaniesQuery;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.*;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static java.util.Collections.singletonList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(SpringRunner.class)
public class CompanyEventHandlerTest {

    private static final String COMPANY_NAME = "the-awesome-company";
    private static final long COMPANY_VALUE = 1337L;
    private static final long AMOUNT_OF_SHARES = 42L;

    private final CompanyViewRepository companyViewRepository = mock(CompanyViewRepository.class);

    private CompanyEventHandler testSubject;

    private CompanyId testCompanyId;

    @Before
    public void setUp() {
        testCompanyId = new CompanyId();

        testSubject = new CompanyEventHandler(companyViewRepository);
    }

    @Test
    public void testOnCompanyCreatedEventACompanyViewIsSaved() {
        CompanyCreatedEvent testEvent =
                new CompanyCreatedEvent(testCompanyId, COMPANY_NAME, COMPANY_VALUE, AMOUNT_OF_SHARES);

        testSubject.on(testEvent);

        ArgumentCaptor<CompanyView> companyViewCaptor = ArgumentCaptor.forClass(CompanyView.class);

        verify(companyViewRepository).save(companyViewCaptor.capture());

        CompanyView result = companyViewCaptor.getValue();
        assertNotNull(result);
        assertEquals(testCompanyId.getIdentifier(), result.getIdentifier());
        assertEquals(COMPANY_NAME, result.getName());
        assertEquals(COMPANY_VALUE, result.getValue());
        assertEquals(AMOUNT_OF_SHARES, result.getAmountOfShares());
    }

    @Test
    public void testFindCompanyByIdReturnsACompanyView() {
        CompanyView testView = new CompanyView();
        testView.setIdentifier(testCompanyId.getIdentifier());
        testView.setName(COMPANY_NAME);
        testView.setValue(COMPANY_VALUE);
        testView.setAmountOfShares(AMOUNT_OF_SHARES);
        testView.setTradeStarted(false);

        when(companyViewRepository.getOne(testCompanyId.getIdentifier())).thenReturn(testView);

        CompanyView result = testSubject.find(new CompanyByIdQuery(testCompanyId));

        assertEquals(testView, result);
    }

    @Test
    public void testFindCompanyByIdReturnsNullForNonExistingCompanyId() {
        when(companyViewRepository.getOne(testCompanyId.getIdentifier())).thenReturn(null);

        Assert.assertNull(testSubject.find(new CompanyByIdQuery(testCompanyId)));
    }

    @Test
    public void testFindAllCompaniesReturnsAllCompanyViews() {
        CompanyView testView = new CompanyView();
        testView.setIdentifier(testCompanyId.getIdentifier());
        testView.setName(COMPANY_NAME);
        testView.setValue(COMPANY_VALUE);
        testView.setAmountOfShares(AMOUNT_OF_SHARES);
        testView.setTradeStarted(false);

        when(companyViewRepository.findAll(any(Pageable.class))).thenReturn(new PageImpl<>(singletonList(testView)));

        List<CompanyView> result = testSubject.find(new FindAllCompaniesQuery(0, 50));

        assertEquals(testView, result.get(0));
    }
}
