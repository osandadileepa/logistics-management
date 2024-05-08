package com.quincus.networkmanagement.impl.preprocessor.calculator;

import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.impl.preprocessor.calculator.impl.GraphNodeCalculatorImpl;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyConnection;
import static com.quincus.networkmanagement.impl.data.NetworkManagementTestData.dummyNode;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class GraphNodeCalculatorTest {

    GraphNodeCalculator graphNodeCalculator = new GraphNodeCalculatorImpl();

    /**
     * Validate we are calculating degree and neighbor correctly.
     * In the example,
     * MNL has multiple round-trip flights to CEB
     * MY serves as a connecting flight for BKK to MNL and vice versa
     * MNL has a direct one-way flight to BKK
     * MNL has one round-trip flight to HK
     */
    @Test
    void returnValidDegreeAndNumberOfNeighbors() {
        Node mnl = dummyNode("MNL");
        Node ceb = dummyNode("CEB");
        Node hk = dummyNode("HK");
        Node my = dummyNode("MY");
        Node bkk = dummyNode("BKK");

        List<Connection> connections = List.of(
                // MNL has multiple round-trip flights to CEB
                dummyConnection(mnl, ceb),
                dummyConnection(mnl, ceb),
                dummyConnection(mnl, ceb),
                dummyConnection(ceb, mnl),
                dummyConnection(ceb, mnl),
                dummyConnection(ceb, mnl),
                // MY serves as a connecting flight for BKK to MNL and vice versa
                dummyConnection(mnl, my),
                dummyConnection(my, mnl),
                dummyConnection(my, bkk),
                dummyConnection(bkk, my),
                // MNL has a direct one-way flight to BKK
                dummyConnection(mnl, bkk),
                // MNL has one round-trip flight to HK
                dummyConnection(mnl, hk),
                dummyConnection(hk, mnl)
        );

        assertThat(graphNodeCalculator.getDegree(mnl, connections)).isEqualTo(11);
        assertThat(graphNodeCalculator.getDegree(ceb, connections)).isEqualTo(6);
        assertThat(graphNodeCalculator.getDegree(hk, connections)).isEqualTo(2);
        assertThat(graphNodeCalculator.getDegree(my, connections)).isEqualTo(4);
        assertThat(graphNodeCalculator.getDegree(bkk, connections)).isEqualTo(3);
        assertThat(graphNodeCalculator.getNumberOfNeighbors(mnl, connections)).isEqualTo(4);
        assertThat(graphNodeCalculator.getNumberOfNeighbors(ceb, connections)).isEqualTo(1);
        assertThat(graphNodeCalculator.getNumberOfNeighbors(hk, connections)).isEqualTo(1);
        assertThat(graphNodeCalculator.getNumberOfNeighbors(my, connections)).isEqualTo(2);
        assertThat(graphNodeCalculator.getNumberOfNeighbors(bkk, connections)).isEqualTo(1);
    }

}
