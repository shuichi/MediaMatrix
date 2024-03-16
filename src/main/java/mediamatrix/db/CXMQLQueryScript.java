package mediamatrix.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CXMQLQueryScript extends CXMQLScript {

    public static final int WORKER_THREAD_SIZE = 2;
    private QueryByClause queryBy;
    private FromClause from;
    private RankByClause rankBy;
    private final CXMQLContext context;
    private final PrimitiveEngine pe;
    private int previous;

    @SuppressWarnings("unchecked")
    public CXMQLQueryScript() {
        super();
        context = new CXMQLContext();
        pe = new PrimitiveEngine();
        context.set("pe", pe);
    }

    public synchronized Object setProperty(String key, String value) {
        return pe.setProperty(key, value);
    }

    public synchronized String getProperty(String key) {
        return pe.getProperty(key);
    }

    public FromClause getFrom() {
        return from;
    }

    public void setFrom(FromClause from) {
        this.from = from;
    }

    public QueryByClause getQueryBy() {
        return queryBy;
    }

    public void setQueryBy(QueryByClause queryBy) {
        this.queryBy = queryBy;
    }

    public RankByClause getRankUsing() {
        return rankBy;
    }

    public void setRankUsing(RankByClause rankUsing) {
        this.rankBy = rankUsing;
    }

    @Override
    public String toString() {
        return queryBy.toString() + from.toString() + rankBy.toString();
    }

    @Override
    public CXMQLResultSet eval() throws Exception {
        int worker = WORKER_THREAD_SIZE;
        if (pe.getProperty(CXMQLParameterNames.WORKER) != null) {
            worker = Integer.parseInt(pe.getProperty(CXMQLParameterNames.WORKER));
        }
        final ExecutorService executor = Executors.newFixedThreadPool(worker);
        final Set<MediaDataObjectScore> result = new TreeSet<MediaDataObjectScore>();
        final List<Future<MediaDataObjectScore>> futures = new ArrayList<Future<MediaDataObjectScore>>();
        queryBy.eval(context);
        from.prepare();

        getPropertyChangeSupport().firePropertyChange("progress", 0, 1);

        for (int i = 0; i < from.size(); i++) {
            final int index = i;
            futures.add(executor.submit(new Callable<MediaDataObjectScore>() {

                @Override
                public MediaDataObjectScore call() throws Exception {
                    from.eval(index, context, pe);
                    rankBy.eval(context);
                    final MediaDataObjectScore score = new MediaDataObjectScore(from.get(index), (Double) context.get("SCORE"));
                    @SuppressWarnings({"unchecked"})
                    final Set<mediamatrix.utils.Score<Integer, Double>> frames = (Set<mediamatrix.utils.Score<Integer, Double>>) context.get("FRAMES");
                    score.setFrameCorrelation(frames);
                    final Set<?> keys = context.keySet();
                    for (final Iterator<?> it = keys.iterator(); it.hasNext();) {
                        final String key = (String) it.next();
                        if (context.get(key) instanceof CorrelationMatrix) {
                            score.putCorrelationMatrix(key, (CorrelationMatrix) context.get(key));
                        }
                    }
                    return score;
                }
            }));
        }

        try {
            for (int i = 0; i < futures.size(); i++) {
                result.add(futures.get(i).get());
                int current = 0;
                if (from.size() - 1 == 0) {
                    current = 100;
                } else {
                    current = 100 * i / (from.size() - 1);
                }
                getPropertyChangeSupport().firePropertyChange("progress", previous, current);
                previous = current;
            }
        } finally {
            executor.shutdown();
        }
        return new CXMQLResultSet(result, getType());
    }

    @Override
    public String getType() {
        return pe.getProperty("TYPE");
    }

    @Override
    public Map<String, Object> getVars() {
        return context;
    }
}
