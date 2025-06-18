package com.simplecity.amp_library.utils;

import com.simplecity.amp_library.model.ArtworkProvider;
import com.simplecity.amp_library.model.UserSelectedArtwork;
import com.simplecity.amp_library.ui.modelviews.ArtworkView;
import com.simplecityapps.recycler_adapter.model.ViewModel;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ArtworkDialogTest {

    @Test
    public void testGetCheckedViewReturnsSelectedArtworkView() {
        List<ViewModel> viewModels = new ArrayList<>();
        ArtworkView view1 = new ArtworkView(ArtworkProvider.Type.FOLDER, null, null, new File("file1"), false);
        ArtworkView view2 = new ArtworkView(ArtworkProvider.Type.FOLDER, null, null, new File("file2"), false);
        view1.setSelected(false);
        view2.setSelected(true);
        viewModels.add(view1);
        viewModels.add(view2);

        ArtworkView checked = ArtworkDialog.getCheckedView(viewModels);
        assertNotNull(checked);
        assertTrue(checked.isSelected());
        assertEquals(view2, checked);
    }

    @Test
    public void testGetCheckedViewReturnsNullIfNoneSelected() {
        List<ViewModel> viewModels = new ArrayList<>();
        ArtworkView view1 = new ArtworkView(ArtworkProvider.Type.FOLDER, null, null, new File("file1"), false);
        view1.setSelected(false);
        viewModels.add(view1);

        ArtworkView checked = ArtworkDialog.getCheckedView(viewModels);
        assertNull(checked);
    }

    @Test
    public void testGetCheckedViewEmptyList() {
        List<ViewModel> viewModels = new ArrayList<>();
        ArtworkView checked = ArtworkDialog.getCheckedView(viewModels);
        assertNull(checked);
    }
}
