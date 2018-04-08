import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

public class RoundUp {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env =
                StreamExecutionEnvironment.getExecutionEnvironment();

        DataStream<Long> dataStream = env
                .socketTextStream("localhost", 9999)
                .filter(new Filter())
                .map(new Round());

        dataStream.print();

        env.execute("Modulo Operator");
    }


    public static class Round implements MapFunction<String, Long> {

        public Long map(String input) throws Exception {
            double d = Double.parseDouble(input.trim());

            return Math.round(d);
        }
    }

    public static class Filter implements FilterFunction<String> {

        public boolean filter(String input) throws Exception {
            try {
                Double.parseDouble(input.trim());

                return true;
            } catch (Exception ex) {
            }

            return false;
        }

    }

}
