import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.connectors.twitter.TwitterSource;
import org.apache.flink.util.Collector;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;


public class TwitterTumblingWindow {

    public static void main(String[] args) throws Exception {

        final ParameterTool params = ParameterTool.fromArgs(args);

        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();

        env.getConfig().setGlobalJobParameters(params);

        if (!params.has(TwitterSource.CONSUMER_KEY) ||
                !params.has(TwitterSource.CONSUMER_SECRET) ||
                !params.has(TwitterSource.TOKEN) ||
                !params.has(TwitterSource.TOKEN_SECRET)) {
            System.out.println("Use --twitter-source.consumerKey <key> "
                    + "--twitter-source.consumerSecret <secret> "
                    + "--twitter-source.token <token> "
                    + "--twitter-source.tokenSecret <tokenSecret> "
                    + "to specify authentication info.");
            System.exit(1);
            return;
        }

        DataStream<String> streamSource = env.addSource(
                new TwitterSource(params.getProperties()));

        DataStream<Tuple2<String, Integer>> tweets = streamSource
                .flatMap(new LanguageCount())
                .keyBy(0)
                .window(TumblingProcessingTimeWindows.of(Time.seconds(10)))
                .sum(1);

        tweets.print();

        env.execute("Twitter Language Count");
    }

    public static class LanguageCount
            implements FlatMapFunction<String, Tuple2<String, Integer>> {

        private transient ObjectMapper jsonParser;

        public void flatMap(String value, Collector<Tuple2<String, Integer>> out)
                throws Exception {

            if(jsonParser == null) {
                jsonParser = new ObjectMapper();
            }

            JsonNode jsonNode = jsonParser.readValue(value, JsonNode.class);

            String language = jsonNode.has("user") && jsonNode.get("user").has("lang")
                    ? jsonNode.get("user").get("lang").getTextValue()
                    : "unknown";

            out.collect(new Tuple2<String, Integer>(language, 1));
        }
    }
}











