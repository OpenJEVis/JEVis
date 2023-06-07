package org.jevis.jecc.application.Chart.data;

import java.util.ArrayList;
import java.util.List;

public class FavoriteAnalyses {

    private final List<FavoriteAnalysis> favoriteAnalyses = new ArrayList<>();

    public List<FavoriteAnalysis> getFavoriteAnalyses() {
        return favoriteAnalyses;
    }

    public void setFavoriteAnalyses(List<FavoriteAnalysis> favoriteAnalyses) {
        this.favoriteAnalyses.clear();
        this.favoriteAnalyses.addAll(favoriteAnalyses);
    }

    public void reset() {
        this.favoriteAnalyses.clear();
    }
}
