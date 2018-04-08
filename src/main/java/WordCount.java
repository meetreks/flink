import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.util.Collector;



public class WordCount {

    public static void main(String[] args) throws Exception {
        final ParameterTool params = ParameterTool.fromArgs(args);

        final StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();

        env.getConfig().setGlobalJobParameters(params);
        DataStream<String> dataStream = StreamUtil.getDataStream(env,params);

        //DataStream<String> dataStream = StreamUtil.getDataStream(env, params);

        if (dataStream == null) {
            System.exit(1);
            return;
        }

        DataStream<Tuple2<String, Integer>> wordCountStream = dataStream
                .flatMap(new WordCountSplitter())
                .keyBy(0)
                .sum(1);

        wordCountStream.print();

        env.execute("Word Count");
    }

    public static class WordCountSplitter implements
            FlatMapFunction<String, Tuple2<String, Integer>> {

        public void flatMap(String sentence, Collector<Tuple2<String, Integer>> out)
                throws Exception {
            for (String word: sentence.split(" ")) {
                out.collect(new Tuple2<String, Integer>(word, 1));
            }
        }
    }

}








