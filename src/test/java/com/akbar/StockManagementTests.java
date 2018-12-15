package com.akbar;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;


public class StockManagementTests {

    private ExternalISBNDataService webService;
    private ExternalISBNDataService databaseService;
    private StockManager stockManager;

    @Before
    public void setup() {
        webService = mock(ExternalISBNDataService.class);
        databaseService = mock(ExternalISBNDataService.class);
        stockManager = new StockManager();
        stockManager.setWebService(webService);
        stockManager.setDatabaseService(databaseService);
    }

    //Using Stubs
    @Test
    public void testCanGetACorrectLocatorCode() {
        ExternalISBNDataService webService = new ExternalISBNDataService() {
            public Book lookup(String isbn) {
                return new Book(isbn, "Of Mice And Men", "J. Steinbeck");
            }
        };

        ExternalISBNDataService databaseService = new ExternalISBNDataService() {
            public Book lookup(String isbn) {
                return null;
            }
        };
        stockManager = new StockManager();
        stockManager.setWebService(webService);
        stockManager.setDatabaseService(databaseService);
        String isbn = "0140177396";
        String locatorCode = stockManager.getLocatorCode(isbn);
        assertEquals("7396J4", locatorCode);
    }

    //Using mock
    @Test
    public void testCanGetACorrectLocatorCode_usingMock() {
        when(webService.lookup(anyString())).thenReturn(new Book("0140177396", "Of Mice And Men", "J. Steinbeck"));
        when(databaseService.lookup(anyString())).thenReturn(null);

        String isbn = "0140177396";
        String locatorCode = stockManager.getLocatorCode(isbn);
        assertEquals("7396J4", locatorCode);
    }

    @Test
    public void databaseIsUsedIfDataIsPresent() {
        when(databaseService.lookup("0974514012")).thenReturn(new Book("0974514012", "Pragmatic Unit Testing", "Andy Hunt"));

        String isbn = "0974514012";
        String locatorCode = stockManager.getLocatorCode(isbn);

        verify(databaseService, times(1)).lookup("0974514012");
        verify(webService, times(0)).lookup(anyString());
    }

    @Test
    public void webserviceIsUsedIfDataIsNotPresentInDatabase() {
        when(databaseService.lookup("0974514012")).thenReturn(null);
        when(webService.lookup("0974514012")).thenReturn(new Book("0974514012", "Pragmatic Unit Testing", "Andy Hunt"));

        String isbn = "0974514012";
        String locatorCode = stockManager.getLocatorCode(isbn);

        verify(databaseService, atLeast(1)).lookup("0974514012"); //using atLeast()
        verify(webService, atMost(1)).lookup("0974514012"); //using atMost()
    }


}
