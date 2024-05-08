package com.quincus.networkmanagement.api.validator;

import com.quincus.networkmanagement.api.constant.TransportType;
import com.quincus.networkmanagement.api.domain.Connection;
import com.quincus.networkmanagement.api.domain.Node;
import com.quincus.networkmanagement.api.domain.VehicleType;
import com.quincus.networkmanagement.api.validator.constraint.ValidConnection;
import org.apache.commons.lang3.StringUtils;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.Optional;

public class ConnectionValidator implements ConstraintValidator<ValidConnection, Connection> {

    @Override
    public boolean isValid(Connection connection, ConstraintValidatorContext context) {
        context.disableDefaultConstraintViolation();

        return isValidNodes(connection, context) && isValidTransportTypeSpecifics(connection, context);
    }

    private boolean isValidNodes(Connection connection, ConstraintValidatorContext context) {
        boolean isValidDepartureNode = isNodeValid(connection.getDepartureNode(), "departureNode", context);
        boolean isValidArrivalNode = isNodeValid(connection.getArrivalNode(), "arrivalNode", context);

        if (isValidDepartureNode && isValidArrivalNode) {
            String departureNodeId = Optional.ofNullable(connection.getDepartureNode()).map(Node::getId).orElse(null);
            String arrivalNodeId = Optional.ofNullable(connection.getArrivalNode()).map(Node::getId).orElse(null);

            String departureNodeCode = Optional.ofNullable(connection.getDepartureNode()).map(Node::getNodeCode).orElse(null);
            String arrivalNodeCode = Optional.ofNullable(connection.getArrivalNode()).map(Node::getNodeCode).orElse(null);

            if (!StringUtils.isBlank(departureNodeId) && !StringUtils.isBlank(arrivalNodeId) && departureNodeId.equals(arrivalNodeId)) {
                buildViolation(context, "departure node id and arrival node id cannot be the same", "arrivalNode.id");
                return false;
            }

            if (!StringUtils.isBlank(departureNodeCode) && !StringUtils.isBlank(arrivalNodeCode) && departureNodeCode.equals(arrivalNodeCode)) {
                buildViolation(context, "departure node code and arrival node code cannot be the same", "arrivalNode.nodeCode");
                return false;
            }
        }

        return isValidDepartureNode && isValidArrivalNode;
    }

    private boolean isNodeValid(Node node, String nodeName, ConstraintValidatorContext context) {
        String nodeId = Optional.ofNullable(node).map(Node::getId).orElse(null);
        String nodeCode = Optional.ofNullable(node).map(Node::getNodeCode).orElse(null);

        if (StringUtils.isAllBlank(nodeCode, nodeId)) {
            buildViolation(context, nodeName + " must have either id or code", nodeName + ".nodeCode");
            return false;
        }

        return true;
    }

    private boolean isValidTransportTypeSpecifics(Connection connection, ConstraintValidatorContext context) {
        if (connection.getTransportType() == TransportType.AIR) {
            return isValidAirConnection(connection, context);
        }
        return isValidGroundConnection(connection, context);
    }

    private boolean isValidAirConnection(Connection connection, ConstraintValidatorContext context) {
        return isNotNull(connection.getAirLockoutDuration(), "air lockout duration is required when transport type is AIR", "airLockoutDuration", context) &&
                isNotNull(connection.getAirRecoveryDuration(), "air recovery duration is required when transport type is AIR", "airRecoveryDuration", context);
    }

    private boolean isValidGroundConnection(Connection connection, ConstraintValidatorContext context) {
        return isValidVehicleType(connection.getVehicleType(), context);
    }

    private boolean isNotNull(Object value, String message, String property, ConstraintValidatorContext context) {
        if (value == null) {
            buildViolation(context, message, property);
            return false;
        }
        return true;
    }

    private boolean isValidVehicleType(VehicleType vehicleType, ConstraintValidatorContext context) {
        boolean isValid = true;
        if (vehicleType == null) {
            isValid = false;
            buildViolation(context, "vehicle type is required when transport type is GROUND", "vehicleType");
        } else if (StringUtils.isAllBlank(vehicleType.getId(), vehicleType.getName())) {
            isValid = false;
            buildViolation(context, "vehicle type must have either id or name", "vehicleType.name");
        }
        return isValid;
    }

    private void buildViolation(ConstraintValidatorContext context, String message, String property) {
        context.buildConstraintViolationWithTemplate(message)
                .addPropertyNode(property)
                .addConstraintViolation();
    }
}
